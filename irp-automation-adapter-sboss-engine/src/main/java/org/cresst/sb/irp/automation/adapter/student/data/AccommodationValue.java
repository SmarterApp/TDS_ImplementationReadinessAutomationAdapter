/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 *
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package org.cresst.sb.irp.automation.adapter.student.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author temp_rreddy
 */
public class AccommodationValue {

    @JsonIgnore
    private AccommodationType _parentType;

    @JsonProperty("code")
    private String _code;

    @JsonProperty("name")
    private String _name;

    @JsonProperty("allowCombine")
    private boolean _allowCombine;

    @JsonProperty("isDefault")
    private boolean _isDefault = false;

    @JsonProperty("selected")
    private boolean _isSelected;

    public AccommodationValue() {
    }

    @JsonIgnore
    public String[] getCodes() {

        return _code.split("&");

    }

    // / <summary>
    // / Is this value selected by default.
    // / </summary>

    /**
     * @return the _parentType
     */
    @JsonIgnore
    public AccommodationType getParentType() {
        return _parentType;
    }

    public void setParentType(AccommodationType parentType) {
        this._parentType = parentType;
    }

    /**
     * @return the _code
     */
    public String getCode() {
        return _code;
    }

    /**
     * @return the _name
     */
    public String getName() {
        return _name;
    }

    /**
     * @return the _allowCombine
     */
    public boolean isAllowCombine() {
        return _allowCombine;
    }

    public boolean getIsDefault() {
        return _isDefault;
    }

    public boolean getIsSelected() {
        return _isSelected;
    }

    public void setIsSelected(boolean value) {
        _isSelected = value;
    }

    public void setIsDefault(boolean value) {
        // we need to make sure there is ever only one default value set
        if (_isDefault != value) {
            // check if isDefault was set to true
            if (value) {
                // set every value as not default except for this one
                if (_parentType != null) {
                    for (AccommodationValue existingValue : this._parentType.getValues()) {
                        if (existingValue != this) {
                            existingValue._isDefault = false;
                        }
                    }
                }

                _isDefault = true;
            } else {
                _isDefault = false;
            }
        }
    }

    public AccommodationValue(AccommodationType parentType, String code, String label, boolean isDefault, boolean allowCombine) {
        _parentType = parentType;
        _code = code;
        _name = label;
        _isDefault = isDefault;
        _allowCombine = allowCombine;
    }

    public boolean equals(AccommodationValue other) {
        // TODO
    /*
     * if (ReferenceEquals(null, other)) return false; if (ReferenceEquals(this,
     * other)) return true; return equals(other.getCode(), getCode());
     */
        if (other == null)
            return false;
        if (other == this)
            return true;
        return other.getCode().equalsIgnoreCase(getCode());

    }

    @Override
    public boolean equals(Object obj) {
    /*
     * if (ReferenceEquals(null, obj)) return false; if (ReferenceEquals(this,
     * obj)) return true; if (obj.GetType() != typeof(AccommodationValue))
     * return false; return equals((AccommodationValue) obj);
     */
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof AccommodationValue))
            return false;
        return equals((AccommodationValue) obj);

    }

    // TODO
    @Override
    public int hashCode() {
        return (_code != null ? _code.hashCode() : 0);
    }

    @Override
    public String toString() {
        // TODO
        return _name + _code;
    }
}
