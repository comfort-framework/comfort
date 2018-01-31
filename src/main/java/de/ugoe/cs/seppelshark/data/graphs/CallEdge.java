/*
 * Copyright (C) 2017 University of Goettingen, Germany
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ugoe.cs.seppelshark.data.graphs;

import com.google.common.base.MoreObjects;
import de.ugoe.cs.seppelshark.data.models.IUnit;
import java.io.Serializable;
import java.util.Comparator;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;



/**
 * @author Fabian Trautsch
 */
public class CallEdge implements Comparable<CallEdge>, Comparator<CallEdge>, Serializable {
    private CallType callType;
    private Integer orderNumber;
    private IUnit caller;
    private IUnit callee;

    public CallEdge(CallType callType, Integer orderNumber, IUnit caller, IUnit callee) {
        this.callType = callType;
        this.orderNumber = orderNumber;
        this.caller = caller;
        this.callee = callee;
    }

    public CallType getCallType() {
        return callType;
    }

    public void setCallType(CallType callType) {
        this.callType = callType;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    public IUnit getCaller() {
        return caller;
    }

    public void setCaller(IUnit caller) {
        this.caller = caller;
    }

    public IUnit getCallee() {
        return callee;
    }

    public void setCallee(IUnit callee) {
        this.callee = callee;
    }

    @Override
    public int compareTo(@Nonnull CallEdge o) {
        return new CompareToBuilder()
                .append(callType, o.callType)
                .append(orderNumber, o.orderNumber)
                .append(caller, o.caller)
                .append(callee, o.callee)
                .toComparison();
    }

    @Override
    public int compare(CallEdge o1, CallEdge o2) {
        return o1.compareTo(o2);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof CallEdge)) {
            return false;
        }

        if(obj == this) {
            return true;
        }

        CallEdge otherNode = (CallEdge) obj;
        return new EqualsBuilder()
                .append(callType, otherNode.callType)
                .append(orderNumber, otherNode.orderNumber)
                .append(caller, otherNode.caller)
                .append(callee, otherNode.callee)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(callType)
                .append(orderNumber)
                .append(caller)
                .append(callee)
                .toHashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("callType", callType)
                .add("orderNumber", orderNumber)
                .add("caller", caller)
                .add("callee", callee)
                .toString();
    }

}
