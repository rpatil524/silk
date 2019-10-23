import React from "react";
import { render, shallow } from 'enzyme';
import MappingRule from '../../../../src/HierarchicalMapping/Containers/MappingRule/MappingRule';
import {
    Button,
    ConfirmationDialog,
    ContextMenu,
    DismissiveButton,
    DisruptiveButton,
    MenuItem,
    Spinner,
} from '@eccenca/gui-elements';
import ObjectMappingRule from '../../../../src/HierarchicalMapping/Containers/MappingRule/ObjectMappingRule/ObjectMappingRule';
import ValueMappingRule from '../../../../src/HierarchicalMapping/Containers/MappingRule/ValueMappingRule';

const onRuleIdChangeFn = jest.fn();
const onExpandFn = jest.fn();
const props = {
    count: 8,
    expanded: false,
    handleClone: jest.fn(),
    handleCopy: jest.fn(),
    id: "birthdate",
    isPasted: false,
    mappingTarget: {
        uri: "<urn:ruleProperty:birthdate>",
        valueType: {},
        isBackwardProperty: false,
        isAttribute: false
    },
    metadata: {
        label: ""
    },
    onAskDiscardChanges: jest.fn(),
    onClickedRemove: jest.fn(),
    onExpand: onExpandFn,
    onRuleIdChange: onRuleIdChangeFn,
    operator: {
        type: "transformInput",
        id: "normalize",
        function: "DateTypeParser",
        inputs: Array(1),
        parameters: {}
    },
    parentId: "12",
    pos: 4,
    provided: {
        placeholder: null,
        dragHandleProps: jest.fn(),
        draggableStyle: {},
        innerRef: jest.fn()
    },
    scrollElementIntoView: jest.fn(),
    scrollIntoView: jest.fn(),
    snapshot: {isDragging: false},
    sourcePaths: ["birthdate"],
    type: "complex"
};

const selectors = {
    NAV_BUTTON: 'button[data-test-id="button-birthdate"]',
    ROW_CLICK: 'div[data-test-id="row-click"]'
};

const getWrapper = (renderer = shallow, args = props) => renderer(
    <MappingRule {...args} />
);

describe("MappingRule Component", () => {
    describe("on component mounted, ", () => {
        let wrapper;
        beforeEach(() => {
            wrapper = getWrapper(shallow);
            wrapper.setState({
                loading: false
            })
        });
        
        it('should loading appear when data not loaded', () => {
            wrapper.setState({
                loading: true
            });
            expect(wrapper.find(Spinner)).toHaveLength(1)
        });
        
        it('should rendered navigation button', () => {
            const wrapper = getWrapper(render);
            expect(wrapper.find(selectors.NAV_BUTTON)).toHaveLength(1)
        });
        
        it('should render ObjectMappingRule component, when rule is expanded and rule is object', () => {
            const wrapper = getWrapper(shallow, {
                ...props,
                type: 'root',
                expanded: true
            });
            expect(wrapper.find(ObjectMappingRule)).toHaveLength(1);
        });
        
        it('should render ValueMappingRule component, when rule is expanded and rule is NOT object', () => {
            const wrapper = getWrapper(shallow, {
                ...props,
                expanded: true
            });
            expect(wrapper.find(ValueMappingRule)).toHaveLength(1);
        });
        
        it('should render reorder context menu, when rule is not expanded', () => {
            expect(wrapper.find(ContextMenu)).toHaveLength(1);
        });
        
        afterEach(() => {
            wrapper.unmount();
        });
    });
    
    describe("on user interaction, ", () => {
        let wrapper;
        beforeEach(() => {
            wrapper = getWrapper(shallow);
            wrapper.setState({
                loading: false
            })
        });
        
        it('should on row click navigate to the rule page, when rule type is object', () => {
            const wrapper = getWrapper(shallow, {
                ...props,
                type: 'object'
            });
            wrapper.find(selectors.ROW_CLICK).simulate('click', {
                stopPropagation: jest.fn()
            });
            expect(onRuleIdChangeFn).toBeCalledWith({
                newRuleId: props.id,
                parentId: props.parentId
            });
        });
    
        it('should on row click expand the rule, when rule type is NOT object', () => {
            wrapper.find(selectors.ROW_CLICK).simulate('click', {
                stopPropagation: jest.fn()
            });
            expect(onExpandFn).toBeCalled();
        });
        
        afterEach(() => {
            wrapper.unmount();
        })
        
    });
});
