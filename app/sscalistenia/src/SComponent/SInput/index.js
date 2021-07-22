import React, { Component } from 'react';
import { View, StyleSheet, ViewStyle, TouchableOpacity, TextInputProps, Animated, TextInput, Platform } from 'react-native';
import { STheme } from '../../SComponent';
import { Variant, TypeVariant } from './variants';
import { Type, TypeType } from './types';
import { Col, TypeCol } from '../SView/cols';
import { CustomStyles, TypeStyles } from './styles';
import SSize from '../SSize';
import { SText } from '../SText';
import { SView } from '../SView';

type typeConfig = {
    style: ViewStyle,
    customStyle: TypeStyles,
    type: TypeType,
    isRequired: Boolean,
    variant: TypeVariant,
    col: TypeCol,
    icon: Component,
    label: String,
}

interface IProps extends TextInputProps {
    style: ViewStyle,
    props: typeConfig,
    onPress: Function,
}
// export type SInputProps = IProps;

export class SInput extends Component<IProps> {
    static TYPE(type: TypeType) { return type };
    constructor(props) {
        super(props);
        this.state = {
            value: "",
            error: false,
            data: {}
        };

    }
    getOption(option) {
        return !this.props.props[option] ? 'default' : this.props.props[option]
    }
    buildStyle() {
        this.style = {
            ...this.props.style,
            ...(this.props.props.style ? this.props.props.style : {}),
            ...(this.props.props.height ? { height: this.props.props.height } : {}),
        }
    }
    verify() {
        if (!this.props.props.isRequired) return true;
        if (!this.getValue()) {
            this.setState({ error: "null" });
            return false;
        }
        if (this.type) {
            if (this.type.verify) {
                if (!this.type.verify(this.getValue())) {
                    this.setState({ error: "null" });
                    return false;
                }
            }
        }
        this.setState({ error: false });
        return true;
    }
    setValue(val) {
        this.setState({ value: val });
    }
    getValue() {
        return this.state.value;
    }
    setData(val) {
        this.setState({ data: { ...this.state.data, ...val } });
    }
    getData() {
        return this.state.data;
    }
    getIcon() {
        if (!this.type) return <View />
        var ITEM = false;
        if (this.props.props.icon) {
            ITEM = this.props.props.icon
        }
        if (this.type.icon) {
            ITEM = this.type.icon
        }
        if (!ITEM) {
            return <View />
        }
        return <SView
            props={{ variant: "center" }}
            style={{
                height: "100%"
            }}>
            {ITEM}
        </SView>
    }
    getStyle = () => {
        return this.customStyle
    }
    getLabel() {
        if (!this.props.props.label) {
            return <View />
        }
        return <SText style={[
            this.customStyle.LabelStyle,
            this.type.style.LabelStyle,
        ]}>{this.props.props.label}</SText>
    }
    render() {
        this.buildStyle();
        this.variant = Variant(this.getOption("variant"));
        this.customStyle = CustomStyles(this.getOption("customStyle"))
        var size = SSize.getSize(this.getOption("col"))
        var type = Type(this.getOption("type"), this);
        this.type = type;
        var Component = View;
        if (this.props.onPress || type.onPress) {
            Component = TouchableOpacity;
        }
        return (
            <Component
                onLayout={(evt) => {
                    this.layout = evt.nativeEvent.layout
                    if (this.props.onLayout) this.props.onLayout(evt);
                }}
                onPress={() => {
                    if (this.props.onPress) this.props.onPress({
                        layout: this.layout
                    });
                    if (type.onPress) type.onPress({
                        layout: this.layout
                    });
                }}
                style={[
                    this.variant.View,
                    this.customStyle.View,
                    type.style.View,
                    (this.state.error ? this.customStyle.error : {}),
                    {
                        ...size,
                        ...this.style,
                    }
                ]} >
                <SView props={{
                    col: "xs-12",
                    direction: "row",
                }} style={{ flex: 1, height: "100%" }}>
                    {this.getIcon()}
                    <TextInput
                        value={this.state.value}
                        {...type.props}
                        {...this.props}
                        onChangeText={(_text) => {
                            var text = _text;
                            if (this.type) {
                                if (this.type.filter) {
                                    text = this.type.filter(text)
                                }
                            }
                            this.state.value = text
                            this.setState({ value: this.state.value })
                            this.verify()
                        }}
                        style={[
                            this.customStyle.InputText,
                            type.style.InputText,
                            {
                                flex: 1,
                                height: "100%",
                                outline: "none",

                            }]}
                        placeholderTextColor={this.customStyle.placeholder.color}

                    />
                </SView>
                {this.getLabel()}
            </Component >
        );
    }
}

SInput.defaultProps = {
    props: {},
    style: {}
};