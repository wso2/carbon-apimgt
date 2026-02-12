package org.wso2.carbon.apimgt.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigurationDto {

    private String name;
    private String label;
    private String type;
    private String tooltip;
    private Object defaultValue;
    private boolean required;
    private boolean mask;
    private List values = new ArrayList<>();
    private boolean multiple;
    private boolean updateDisabled = false;
    private ConstraintConfigDto constraint;

    public ConfigurationDto withConstraint(AppConfigConstraintType constraintType, Map<String, Object> defaultConstraints,
                                           String label, String tooltip) {
        this.constraint = new ConstraintConfigDto(
            this.name,
            this.type,
            this.values,
            true,
            label,
            tooltip,
            constraintType,
            defaultConstraints
        );
        return this;
    }

    public boolean hasConstraint() {
        return this.constraint != null;
    }

    public ConstraintConfigDto getConstraint() {
        return this.constraint;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getLabel() {

        return label;
    }

    public void setLabel(String label) {

        this.label = label;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public String getTooltip() {

        return tooltip;
    }

    public void setTooltip(String tooltip) {

        this.tooltip = tooltip;
    }

    public Object getDefaultValue() {

        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {

        this.defaultValue = defaultValue;
    }

    public boolean isRequired() {

        return required;
    }

    public void setRequired(boolean required) {

        this.required = required;
    }

    public boolean isMask() {

        return mask;
    }

    public void setMask(boolean mask) {

        this.mask = mask;
    }

    public List<Object> getValues() {

        return values;
    }

    public void setValues(List<Object> values) {

        this.values = values;
    }

    public boolean isMultiple() {

        return multiple;
    }

    public void setMultiple(boolean multiple) {

        this.multiple = multiple;
    }

    public void addValue(String value) {

        this.values.add(value);
    }

    public boolean isUpdateDisabled() {

        return updateDisabled;
    }

    public void setUpdateDisabled(boolean updateDisabled) {

        this.updateDisabled = updateDisabled;
    }

    /**
     * @deprecated Use {@link #ConfigurationDto(String, String, String, String, Object, boolean, boolean, List,
     * boolean, boolean)} instead.
     */
    @Deprecated
    public ConfigurationDto(String name, String label, String type, String tooltip, Object defaultValue,
                            boolean required,
                            boolean mask, List values, boolean multiple) {

        this(name, label, type, tooltip, defaultValue, required, mask, values, multiple, false);
    }

    /**
     * @deprecated Use {@link #ConfigurationDto(String, String, String, String, Object, boolean, boolean,
     * List, boolean, boolean)} instead.
     */
    @Deprecated
    public ConfigurationDto(String name, String label, String type, String tooltip, String defaultValue,
                            boolean required,
                            boolean mask, List values, boolean multiple) {

        this(name, label, type, tooltip, defaultValue, required, mask, values, multiple, false);
    }

    public ConfigurationDto(String name, String label, String type, String tooltip, Object defaultValue,
                            boolean required,
                            boolean mask, List values, boolean multiple, boolean updateDisabled) {

        this.name = name;
        this.label = label;
        this.type = type;
        this.tooltip = tooltip;
        this.defaultValue = defaultValue;
        this.required = required;
        this.mask = mask;
        this.values = values;
        this.multiple = multiple;
        this.updateDisabled = updateDisabled;
    }
}
