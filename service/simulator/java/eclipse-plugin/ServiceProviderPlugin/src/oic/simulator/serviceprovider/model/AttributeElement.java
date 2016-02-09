/*
 * Copyright 2015 Samsung Electronics All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package oic.simulator.serviceprovider.model;

import java.util.HashMap;
import java.util.Map;

import org.oic.simulator.ArrayProperty;
import org.oic.simulator.AttributeProperty;
import org.oic.simulator.AttributeValue;
import org.oic.simulator.DoubleProperty;
import org.oic.simulator.IntegerProperty;
import org.oic.simulator.InvalidArgsException;
import org.oic.simulator.ModelProperty;
import org.oic.simulator.SimulatorResourceAttribute;
import org.oic.simulator.SimulatorResourceModel;
import org.oic.simulator.StringProperty;
import org.oic.simulator.server.SimulatorResource.AutoUpdateType;

import oic.simulator.serviceprovider.manager.UiListenerHandler;
import oic.simulator.serviceprovider.utils.AttributeValueStringConverter;
import oic.simulator.serviceprovider.utils.Constants;

public class AttributeElement {
    private Object                        mParent             = null;
    private SimulatorResourceAttribute    mAttribute          = null;
    private Map<String, AttributeElement> mChildAttributes    = new HashMap<String, AttributeElement>();
    private boolean                       mAutoUpdateSupport  = false;
    private int                           mAutoUpdateId       = -1;
    private boolean                       mAutoUpdateState    = false;
    private int                           mAutoUpdateInterval = Constants.DEFAULT_AUTOMATION_INTERVAL;
    private AutoUpdateType                mAutoUpdateType     = Constants.DEFAULT_AUTOMATION_TYPE;
    private boolean                       mEditLock           = false;

    public AttributeElement(Object parent,
            SimulatorResourceAttribute attribute, boolean autoUpdateSupport)
            throws NullPointerException {
        mParent = parent;
        mAttribute = attribute;
        mAutoUpdateSupport = autoUpdateSupport;
        AttributeValue.TypeInfo typeInfo = attribute.value().typeInfo();
        if (typeInfo.mType == AttributeValue.ValueType.RESOURCEMODEL) {
            mAutoUpdateSupport = false;
            SimulatorResourceModel resModel = (SimulatorResourceModel) attribute
                    .value().get();
            ModelProperty modelProp = null;
            if (null != attribute.property()) {
                modelProp = attribute.property().asModel();
            }
            String attName;
            for (Map.Entry<String, AttributeValue> entry : resModel.get()
                    .entrySet()) {
                attName = entry.getKey();
                AttributeProperty prop = null;
                if (null != modelProp)
                    prop = modelProp.get(attName);
                mChildAttributes.put(
                        attName,
                        new AttributeElement(this,
                                new SimulatorResourceAttribute(attName, entry
                                        .getValue(), prop), false));
            }
        } else if (typeInfo.mType == AttributeValue.ValueType.ARRAY) {
            mAutoUpdateSupport = false;
            if (typeInfo.mBaseType == AttributeValue.ValueType.RESOURCEMODEL) {
                if (typeInfo.mDepth == 1) {
                    SimulatorResourceModel[] resModelArray = (SimulatorResourceModel[]) attribute
                            .value().get();

                    ArrayProperty arrProp = null;
                    ModelProperty modelProp = null;
                    if (null != attribute.property()) {
                        arrProp = attribute.property().asArray();
                        if (null != arrProp
                                && null != arrProp.getElementProperty()) {
                            modelProp = arrProp.getElementProperty().asModel();
                        }
                    }
                    for (int i = 0; i < resModelArray.length; i++) {
                        SimulatorResourceAttribute indexAttribute = new SimulatorResourceAttribute(
                                "[" + Integer.toString(i) + "]",
                                new AttributeValue(resModelArray[i]), modelProp);
                        mChildAttributes.put("[" + Integer.toString(i) + "]",
                                new AttributeElement(this, indexAttribute,
                                        false));
                    }
                } else if (typeInfo.mDepth == 2) {
                    SimulatorResourceModel[][] resModelArray = (SimulatorResourceModel[][]) attribute
                            .value().get();

                    ArrayProperty arrProp = null;
                    ArrayProperty arrChildPropLevel1 = null;
                    ModelProperty modelProp = null;
                    if (null != attribute.property()) {
                        arrProp = attribute.property().asArray();
                        if (null != arrProp
                                && null != arrProp.getElementProperty()) {
                            arrChildPropLevel1 = arrProp.getElementProperty()
                                    .asArray();
                            if (null != arrChildPropLevel1
                                    && null != arrChildPropLevel1
                                            .getElementProperty()) {
                                modelProp = arrProp.getElementProperty()
                                        .asModel();
                            }
                        }
                    }

                    for (int i = 0; i < resModelArray.length; i++) {
                        SimulatorResourceAttribute indexAttribute = new SimulatorResourceAttribute(
                                "[" + Integer.toString(i) + "]",
                                new AttributeValue(resModelArray[i]), modelProp);
                        mChildAttributes.put("[" + Integer.toString(i) + "]",
                                new AttributeElement(this, indexAttribute,
                                        false));
                    }
                } else if (typeInfo.mDepth == 3) {
                    SimulatorResourceModel[][][] resModelArray = (SimulatorResourceModel[][][]) attribute
                            .value().get();

                    ArrayProperty arrProp = null;
                    ArrayProperty arrChildPropLevel1 = null;
                    ArrayProperty arrChildPropLevel2 = null;
                    ModelProperty modelProp = null;
                    if (null != attribute.property()) {
                        arrProp = attribute.property().asArray();
                        if (null != arrProp
                                && null != arrProp.getElementProperty()) {
                            arrChildPropLevel1 = arrProp.getElementProperty()
                                    .asArray();
                            if (null != arrChildPropLevel1
                                    && null != arrChildPropLevel1
                                            .getElementProperty()) {
                                arrChildPropLevel2 = arrChildPropLevel1
                                        .getElementProperty().asArray();
                                if (null != arrChildPropLevel2
                                        && null != arrChildPropLevel2
                                                .getElementProperty()) {
                                    modelProp = arrChildPropLevel2
                                            .getElementProperty().asModel();
                                }
                            }
                        }
                    }

                    for (int i = 0; i < resModelArray.length; i++) {
                        SimulatorResourceAttribute indexAttribute = new SimulatorResourceAttribute(
                                "[" + Integer.toString(i) + "]",
                                new AttributeValue(resModelArray[i]), modelProp);
                        mChildAttributes.put("[" + Integer.toString(i) + "]",
                                new AttributeElement(this, indexAttribute,
                                        false));
                    }
                }
            }
        }
    }

    public Object getParent() {
        return mParent;
    }

    public boolean hasChildren() {
        if (mChildAttributes != null && mChildAttributes.size() > 0)
            return true;
        return false;
    }

    public Map<String, AttributeElement> getChildren() {
        if (hasChildren() == true)
            return mChildAttributes;
        return null;
    }

    public SimulatorResourceAttribute getSimulatorResourceAttribute() {
        return mAttribute;
    }

    public int getAutoUpdateId() {
        return mAutoUpdateId;
    }

    public void setAutoUpdateId(int id) {
        mAutoUpdateId = id;
    }

    public boolean isAutoUpdateInProgress() {
        return mAutoUpdateState;
    }

    public void setAutoUpdateState(boolean state) {
        if (mAutoUpdateState != state) {
            mAutoUpdateState = state;

            UiListenerHandler.getInstance()
                    .attributeUpdatedUINotification(this);
        }
    }

    public int getAutoUpdateInterval() {
        return mAutoUpdateInterval;
    }

    public void setAutoUpdateInterval(int interval) {
        mAutoUpdateInterval = interval;
    }

    public AutoUpdateType getAutoUpdateType() {
        return mAutoUpdateType;
    }

    public void setAutoUpdateType(AutoUpdateType type) {
        mAutoUpdateType = type;
    }

    public boolean isAutoUpdateSupport() {
        return mAutoUpdateSupport;
    }

    public boolean isReadOnly() {
        AttributeProperty prop = mAttribute.property();
        if (null == prop) {
            return true;
        }

        if (prop.isInteger()) {
            IntegerProperty intProperty = prop.asInteger();
            return !(intProperty.hasRange() || intProperty.hasValues());
        }

        if (prop.isDouble()) {
            DoubleProperty dblProperty = prop.asDouble();
            return !(dblProperty.hasRange() || dblProperty.hasValues());
        }

        if (prop.isBoolean()) {
            return false;
        }

        if (prop.isString()) {
            StringProperty strProperty = prop.asString();
            return !(strProperty.hasValues());
        }

        return true;
    }

    public synchronized boolean getEditLock() {
        return mEditLock;
    }

    public synchronized void setEditLock(boolean editLock) {
        this.mEditLock = editLock;
    }

    public void update(SimulatorResourceAttribute attribute) {
        if (attribute == null)
            return;

        AttributeValue.TypeInfo typeInfo = attribute.value().typeInfo();
        if (typeInfo.mType == AttributeValue.ValueType.RESOURCEMODEL) {
            SimulatorResourceModel resModel = (SimulatorResourceModel) attribute
                    .value().get();
            for (Map.Entry<String, AttributeValue> entry : resModel.get()
                    .entrySet()) {
                AttributeElement attributeElement = mChildAttributes.get(entry
                        .getKey());
                if (attributeElement != null) {
                    attributeElement.update(new SimulatorResourceAttribute(
                            entry.getKey(), entry.getValue()));
                } else {
                    // Display new attribute in UI
                    AttributeElement newAttribute = new AttributeElement(this,
                            new SimulatorResourceAttribute(entry.getKey(),
                                    entry.getValue()), false);
                    mChildAttributes.put(entry.getKey(), newAttribute);

                    UiListenerHandler.getInstance()
                            .attributeAddedUINotification(newAttribute);
                }
            }
        } else if (typeInfo.mType == AttributeValue.ValueType.ARRAY
                && typeInfo.mBaseType == AttributeValue.ValueType.RESOURCEMODEL) {
            if (typeInfo.mDepth == 1) {
                SimulatorResourceModel[] resModelArray = (SimulatorResourceModel[]) attribute
                        .value().get();
                for (int i = 0; i < resModelArray.length; i++) {
                    SimulatorResourceAttribute indexAttribute = new SimulatorResourceAttribute(
                            "[" + Integer.toString(i) + "]",
                            new AttributeValue(resModelArray[i]), null);
                    AttributeElement attributeElement = mChildAttributes
                            .get("[" + Integer.toString(i) + "]");
                    if (attributeElement != null) {
                        attributeElement.update(indexAttribute);
                    } else {
                        // Display new attribute in UI
                        AttributeElement newAttribute = new AttributeElement(
                                this, indexAttribute, false);
                        mChildAttributes.put("[" + Integer.toString(i) + "]",
                                newAttribute);
                        UiListenerHandler.getInstance()
                                .attributeAddedUINotification(newAttribute);
                    }
                }
            }
            if (typeInfo.mDepth == 2) {
                SimulatorResourceModel[][] resModelArray = (SimulatorResourceModel[][]) attribute
                        .value().get();
                for (int i = 0; i < resModelArray.length; i++) {
                    SimulatorResourceAttribute indexAttribute = new SimulatorResourceAttribute(
                            "[" + Integer.toString(i) + "]",
                            new AttributeValue(resModelArray[i]), null);
                    AttributeElement attributeElement = mChildAttributes
                            .get("[" + Integer.toString(i) + "]");
                    if (attributeElement != null) {
                        attributeElement.update(indexAttribute);
                    } else {
                        // Display new attribute in UI
                        AttributeElement newAttribute = new AttributeElement(
                                this, indexAttribute, false);
                        mChildAttributes.put("[" + Integer.toString(i) + "]",
                                newAttribute);
                        UiListenerHandler.getInstance()
                                .attributeAddedUINotification(newAttribute);
                    }
                }
            }
            if (typeInfo.mDepth == 3) {
                SimulatorResourceModel[][][] resModelArray = (SimulatorResourceModel[][][]) attribute
                        .value().get();
                for (int i = 0; i < resModelArray.length; i++) {
                    SimulatorResourceAttribute indexAttribute = new SimulatorResourceAttribute(
                            "[" + Integer.toString(i) + "]",
                            new AttributeValue(resModelArray[i]), null);
                    AttributeElement attributeElement = mChildAttributes
                            .get("[" + Integer.toString(i) + "]");
                    if (attributeElement != null) {
                        attributeElement.update(indexAttribute);
                    } else {
                        // Display new attribute in UI
                        AttributeElement newAttribute = new AttributeElement(
                                this, indexAttribute, false);
                        mChildAttributes.put("[" + Integer.toString(i) + "]",
                                newAttribute);
                        UiListenerHandler.getInstance()
                                .attributeAddedUINotification(newAttribute);
                    }
                }
            }
        } else {
            String currentValue = new AttributeValueStringConverter(
                    mAttribute.value()).toString();
            String newValue = new AttributeValueStringConverter(
                    attribute.value()).toString();
            if (!currentValue.equals(newValue)) {
                mAttribute.setValue(attribute.value());
                UiListenerHandler.getInstance().attributeUpdatedUINotification(
                        this);
            }
        }
    }

    public void deepSetChildValue(SimulatorResourceAttribute attribute)
            throws InvalidArgsException {
        if (null == attribute || null == attribute.name())
            return;

        AttributeValue.TypeInfo myValuetypeInfo = mAttribute.value().typeInfo();
        if (myValuetypeInfo.mType == AttributeValue.ValueType.RESOURCEMODEL) {
            SimulatorResourceModel resModel = (SimulatorResourceModel) mAttribute
                    .value().get();
            if (resModel.contains(attribute.name()))
                resModel.set(attribute.name(), attribute.value());
            else
                return;
        }

        if (mParent instanceof AttributeElement)
            ((AttributeElement) mParent).deepSetChildValue(mAttribute);
    }
}
