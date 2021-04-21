package controllers.transform.autoCompletion

import play.api.libs.json.{Format, Json}

/**
  * Request payload for partial source path auto-completion, i.e. suggest replacements for only parts of a more complex source path.
  *
  * @param inputString    The currently entered source path string.
  * @param cursorPosition The cursor position inside the source path string.
  * @param maxSuggestions The max. number of suggestions to return.
  */
case class PartialSourcePathAutoCompletionRequest(inputString: String,
                                                  cursorPosition: Int,
                                                  maxSuggestions: Option[Int]) {
  /** The path until the cursor position. */
  def pathUntilCursor: String = inputString.take(cursorPosition)
  def charBeforeCursor: Option[Char] = pathUntilCursor.reverse.headOption

  private val operatorStartChars = Set('/', '\\', '[')

  /** The remaining characters from the cursor position to the end of the current path operator. */
  def remainingStringInOperator: String = {
    val positionStatus = cursorPositionStatus
    inputString
      .substring(cursorPosition)
      .takeWhile { char =>
        positionStatus.update(char)
        positionStatus.insideQuotesOrUri || !operatorStartChars.contains(char)
      }
  }

  class PositionStatus(initialInsideQuotes: Boolean, initialInsideUri: Boolean) {
    private var _insideQuotes = initialInsideQuotes
    private var _insideUri = initialInsideUri

    def update(char: Char): (Boolean, Boolean) = {
      if(char == '"' && !_insideUri) {
        _insideQuotes = !_insideQuotes
      } else if(char == '<' && !_insideQuotes) {
        _insideUri = true
      } else if(char == '>' && !_insideQuotes) {
        _insideUri = false
      }
      (_insideQuotes, _insideUri)
    }

    def insideQuotes: Boolean = _insideQuotes
    def insideUri: Boolean = _insideUri
    def insideQuotesOrUri: Boolean = insideQuotes || insideUri
  }

  // Checks if the cursor position is inside quotes or URI
  private def cursorPositionStatus: PositionStatus = {
    val positionStatus = new PositionStatus(false, false)
    inputString.take(cursorPosition).foreach(positionStatus.update)
    positionStatus
  }

  /** The index of the operator end, i.e. index in the input string from the cursor to the end of the current operator. */
  def indexOfOperatorEnd: Int = {
    cursorPosition + remainingStringInOperator.length
  }
}

object PartialSourcePathAutoCompletionRequest {
  implicit val partialSourcePathAutoCompletionRequestFormat: Format[PartialSourcePathAutoCompletionRequest] = Json.format[PartialSourcePathAutoCompletionRequest]
}

/**
  * The response for a partial source path auto-completion request.
  * @param inputString         The input string from the request for validation.
  * @param cursorPosition      The cursor position from the request for validation.
  */
case class PartialSourcePathAutoCompletionResponse(inputString: String,
                                                   cursorPosition: Int,
                                                   replacementResults: Seq[ReplacementResults])

/**
  * Suggested replacement for a specific part of the input string.
  *
  * @param replacementInterval An optional interval if there has been found a part of the source path that can be replaced.
  * @param replacements  The auto-completion results.
  * @param extractedQuery A query that has been extracted from around the cursor position that was used for the fil;tering of results.
  */
case class ReplacementResults(replacementInterval: ReplacementInterval,
                              extractedQuery: String,
                              replacements: Seq[CompletionBase])

/** The part of a string to replace.
  *
  * @param from The start index of the string to be replaced.
  * @param length The length in characters that should be replaced.
  */
case class ReplacementInterval(from: Int, length: Int)

object PartialSourcePathAutoCompletionResponse {
  implicit val ReplacementIntervalFormat: Format[ReplacementInterval] = Json.format[ReplacementInterval]
  implicit val ReplacementResultsFormat: Format[ReplacementResults] = Json.format[ReplacementResults]
  implicit val partialSourcePathAutoCompletionResponseFormat: Format[PartialSourcePathAutoCompletionResponse] = Json.format[PartialSourcePathAutoCompletionResponse]
}

