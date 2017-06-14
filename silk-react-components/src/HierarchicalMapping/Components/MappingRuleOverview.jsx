/*
 Whole overview over a hierarchical Mapping on the right, header may be defined here, loops over MappingRule
 */

import React from 'react';
import UseMessageBus from '../UseMessageBusMixin';
import hierarchicalMappingChannel from '../store';
import _ from 'lodash';
import ObjectMappingRuleForm from './MappingRule/Forms/ObjectMappingRuleForm'
import ValueMappingRuleForm from './MappingRule/Forms/ValueMappingRuleForm'
import MappingRuleOverviewHeader from './MappingRuleOverviewHeader';
import MappingRule from './MappingRule/MappingRule';
import {Spinner, Info, ContextMenu, MenuItem} from 'ecc-gui-elements';

const MappingRuleOverview = React.createClass({

    mixins: [UseMessageBus],

    // define property types
    propTypes: {
        currentRuleId: React.PropTypes.string, // selected rule id
    },
    onRuleCreate({type}) {
        this.setState({
            ruleEditView: {
                type,
            },
        });
    },
    handleRuleEditClose() {
        this.setState({
            ruleEditView: false,
        });
    },
    // initilize state
    getInitialState() {
        return {
            loading: true,
            ruleData: {},
            ruleEditView: false
        };
    },
    componentDidMount() {
        this.loadData();
        this.subscribe(hierarchicalMappingChannel.subject('reload'), this.loadData);
        this.subscribe(hierarchicalMappingChannel.subject('ruleId.create'), this.onRuleCreate);
        this.subscribe(hierarchicalMappingChannel.subject('ruleView.unchanged'), this.handleRuleEditClose);
    },
    componentDidUpdate(prevProps) {
        if (prevProps.currentRuleId !== this.props.currentRuleId) {
            this.loadData();
        }
    },
    loadData() {
        // TODO: fix conditions
        this.setState({
            loading: true,
        });

        console.warn('DATA RELOAD')

        hierarchicalMappingChannel.request(
            {
                topic: 'rule.get',
                data: {
                    id: this.props.currentRuleId,
                }
            }
        )
            .subscribe(
                ({rule}) => {
                    this.setState({
                        loading: false,
                        ruleData: rule,
                    });
                },
                (err) => {
                    console.warn('err MappingRuleOverview: rule.get');
                    this.setState({loading: false});
                }
            );
    },
    // sends event to expand / collapse all mapping rules
    handleToggleRuleDetails({expanded}) {
        hierarchicalMappingChannel.subject('rulesView.toggle').onNext({expanded});
    },
    // jumps to selected rule as new center of view
    handleCreate({type}) {
        hierarchicalMappingChannel.subject('ruleId.create').onNext({
            type,
            //FIXME: do we need more data like id of parent as source?
        });
    },
    shouldComponentUpdate(nextProps, nextState) {
        return !_.isEmpty(nextState.ruleData);
    },
    // template rendering
    render () {
        const {
            rules = {},
            id,
        } = this.state.ruleData;

        const createType = _.get(this.state, 'ruleEditView.type', false);
        const createRuleForm = createType ? (
            <div className="ecc-silk-mapping__createrule">
                {
                    createType === 'object' ? (
                        <ObjectMappingRuleForm
                            type={createType}
                            parentId={this.state.ruleData.id}
                            parentName={_.get(this, 'state.ruleData.mappingTarget.uri', '')}
                            edit={true}
                        />
                    ) : (
                        <ValueMappingRuleForm
                            type={createType}
                            onClose={this.handleRuleEditClose}
                            parentId={this.state.ruleData.id}
                            parentName={_.get(this, 'state.ruleData.mappingTarget.uri', '')}
                            edit={true}
                        />
                    )
                }
            </div>
        ) : false;

        const childRules = rules.propertyRules || [];

        const loading = this.state.loading ? <Spinner /> : false;

        let mappingRulesListHead = false;
        let mappingRulesList = false;

        if (!createRuleForm) {
            mappingRulesListHead = (
                <div className="mdl-card__title mdl-card--border">
                    <div className="mdl-card__title-text">
                        Mapping rules {`(${childRules.length})`}
                    </div>
                    <ContextMenu
                        className="ecc-silk-mapping__ruleslistmenu"
                    >
                        <MenuItem
                            className="ecc-silk-mapping__ruleslistmenu__item-add-value"
                            onClick={() => {
                                this.handleCreate({type: 'direct'});
                            }}
                        >
                            Add value mapping
                        </MenuItem>
                        <MenuItem
                            className="ecc-silk-mapping__ruleslistmenu__item-add-object"
                            onClick={() => {
                                this.handleCreate({type: 'object'});
                            }}
                        >
                            Add object mapping
                        </MenuItem>
                        <MenuItem
                            className="ecc-silk-mapping__ruleslistmenu__item-autosuggest"
                        >
                            Suggest rules (0) (TODO)
                        </MenuItem>
                        <MenuItem
                            className="ecc-silk-mapping__ruleslistmenu__item-expand"
                            onClick={() => {
                                this.handleToggleRuleDetails({expanded: true})
                            }}
                        >
                            Expand all
                        </MenuItem>
                        <MenuItem
                            className="ecc-silk-mapping__ruleslistmenu__item-reduce"
                            onClick={() => {
                                this.handleToggleRuleDetails({expanded: false})
                            }}
                        >
                            Reduce all
                        </MenuItem>
                    </ContextMenu>
                </div>

            );

            mappingRulesList = (
                _.isEmpty(childRules) ? (
                    <div className="mdl-card__content">
                        <Info vertSpacing border>
                            No existing mapping rules.
                        </Info>
                        {
                            /* TODO: we should provide options like adding rules or suggestions here,
                             even a help text would be a good support for the user.
                             */
                        }
                    </div>
                ) : (
                    <ol className="mdl-list">
                        {
                            _.map(childRules, (rule, idx) =>
                                (
                                    <MappingRule
                                        pos={idx}
                                        parent={this.props.currentRuleId}
                                        count={childRules.length}
                                        key={`MappingRule_${rule.id}_${idx}`}
                                        {...rule}
                                    />
                                )
                            )
                        }
                    </ol>
                )
            );
        }

        return (
            <div className="ecc-silk-mapping__rules">
                {loading}
                <MappingRuleOverviewHeader rule={this.state.ruleData} key={id}/>
                {
                    createRuleForm ?
                        createRuleForm :
                        <div className="ecc-silk-mapping__ruleslist">
                            <div className="mdl-card mdl-card--stretch mdl-shadow--2dp">
                                {mappingRulesListHead}
                                {mappingRulesList}
                            </div>
                        </div>
                }
            </div>
        );
    },
});

export default MappingRuleOverview;
