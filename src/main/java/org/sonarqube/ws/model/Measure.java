/*
 * SonarQube PDF Report
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarqube.ws.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

public class Measure implements Model {

	private String key;
	private String name;
	private Double val;
	private String frmt_val;

	private String alert;
	private String data;
	private String alert_text;
	private Integer trend;
	private Integer var;
	private Double var1;
	private Double var2;
	private Double var3;
	private Double var4;
	private Double var5;
	private String fvar1;
	private String fvar2;
	private String fvar3;
	private String fvar4;
	private String fvar5;

	private Double variation1;
	private Double variation2;
	private Double variation3;
	private Double variation4;
	private Double variation5;

	@CheckForNull
	public String getKey() {
		return key;
	}

	public Measure setKey(@Nullable String key) {
		this.key = key;
		return this;
	}

	@CheckForNull
	public Double getVal() {
		return val;
	}

	@CheckForNull
	public Integer getIntValue() {
		if (val == null) {
			return null;
		}
		return val.intValue();
	}

	@CheckForNull
	public Long getLongValue() {
		if (val == null) {
			return null;
		}
		return val.longValue();
	}

	public Measure setValue(@Nullable Double val) {
		this.val = val;
		return this;
	}

	@CheckForNull
	public String getFrmt_val() {
		return frmt_val;
	}

	public Measure setFrmt_val(String frmt_val) {
		this.frmt_val = frmt_val;
		return this;
	}

	@CheckForNull
	public String getFrmt_val(@Nullable String defaultValue) {
		if (frmt_val == null) {
			return defaultValue;
		}
		return frmt_val;
	}

	@CheckForNull
	public String getAlert() {
		return alert;
	}

	public Measure setAlert(@Nullable String alert) {
		this.alert = alert;
		return this;
	}

	@CheckForNull
	public String getAlert_text() {
		return alert_text;
	}

	public Measure setAlert_text(@Nullable String alert_text) {
		this.alert_text = alert_text;
		return this;
	}

	@CheckForNull
	public Integer getTrend() {
		return trend;
	}

	public Measure setTrend(@Nullable Integer trend) {
		this.trend = trend;
		return this;
	}

	@CheckForNull
	public Integer getVar() {
		return var;
	}

	public Measure setVar(@Nullable Integer var) {
		this.var = var;
		return this;
	}

	/**
	 * Variation value on period 1. The value is loaded if
	 * ResourceQuery#setIncludeTrends() is set to true.
	 * 
	 * @since 2.5
	 */
	@CheckForNull
	public Double getVariation1() {
		return variation1;
	}

	public Double getVar1() {
		return var1;
	}

	public void setVar1(Double var1) {
		this.var1 = var1;
	}

	public Double getVar2() {
		return var2;
	}

	public void setVar2(Double var2) {
		this.var2 = var2;
	}

	public Double getVar3() {
		return var3;
	}

	public void setVar3(Double var3) {
		this.var3 = var3;
	}

	public Double getVar4() {
		return var4;
	}

	public void setVar4(Double var4) {
		this.var4 = var4;
	}

	public Double getVar5() {
		return var5;
	}

	public void setVar5(Double var5) {
		this.var5 = var5;
	}

	public String getFvar1() {
		return fvar1;
	}

	public void setFvar1(String fvar1) {
		this.fvar1 = fvar1;
	}

	public String getFvar2() {
		return fvar2;
	}

	public void setFvar2(String fvar2) {
		this.fvar2 = fvar2;
	}

	public String getFvar3() {
		return fvar3;
	}

	public void setFvar3(String fvar3) {
		this.fvar3 = fvar3;
	}

	public String getFvar4() {
		return fvar4;
	}

	public void setFvar4(String fvar4) {
		this.fvar4 = fvar4;
	}

	public String getFvar5() {
		return fvar5;
	}

	public void setFvar5(String fvar5) {
		this.fvar5 = fvar5;
	}

	public void setVal(Double val) {
		this.val = val;
	}

	/**
	 * @since 2.5
	 */
	public Measure setVariation1(@Nullable Double variation1) {
		this.variation1 = variation1;
		return this;
	}

	/**
	 * Variation value on period 2. The value is loaded if
	 * ResourceQuery#setIncludeTrends() is set to true.
	 * 
	 * @since 2.5
	 */
	@CheckForNull
	public Double getVariation2() {
		return variation2;
	}

	/**
	 * @since 2.5
	 */
	public Measure setVariation2(@Nullable Double variation2) {
		this.variation2 = variation2;
		return this;
	}

	/**
	 * Variation value on period 3. The value is loaded if
	 * ResourceQuery#setIncludeTrends() is set to true.
	 * 
	 * @since 2.5
	 */
	@CheckForNull
	public Double getVariation3() {
		return variation3;
	}

	/**
	 * @since 2.5
	 */
	public Measure setVariation3(@Nullable Double variation3) {
		this.variation3 = variation3;
		return this;
	}

	/**
	 * Variation value on period 4. The value is loaded if
	 * ResourceQuery#setIncludeTrends() is set to true.
	 * 
	 * @since 2.5
	 */
	@CheckForNull
	public Double getVariation4() {
		return variation4;
	}

	/**
	 * @since 2.5
	 */
	public Measure setVariation4(@Nullable Double variation4) {
		this.variation4 = variation4;
		return this;
	}

	/**
	 * Variation value on period 5. The value is loaded if
	 * ResourceQuery#setIncludeTrends() is set to true.
	 * 
	 * @since 2.5
	 */
	@CheckForNull
	public Double getVariation5() {
		return variation5;
	}

	/**
	 * @since 2.5
	 */
	public Measure setVariation5(@Nullable Double variation5) {
		this.variation5 = variation5;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}