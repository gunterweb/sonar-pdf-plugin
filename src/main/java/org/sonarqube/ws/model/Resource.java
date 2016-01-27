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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import org.sonar.report.pdf.util.MetricKeys;

import com.google.gson.annotations.SerializedName;

/**
 * Resource model
 *
 */
public class Resource implements Model {

    /**
     * 
     */
    private static final long serialVersionUID = -5623549419061461867L;
    private Integer id;
    private String key;
    private String name;
    private String longName;
    private String scope;
    private String qualifier;
    private String language;
    private String version;
    private Integer copy;
    private String description;
    private Date date;
    private List<Measure> msr;
    private Date creationDate;

    // periods used for variations and tracking of violations
    @SerializedName("p1")
    private String period1Mode;
    @SerializedName("p2")
    private String period2Mode;
    @SerializedName("p3")
    private String period3Mode;
    @SerializedName("p4")
    private String period4Mode;
    @SerializedName("p5")
    private String period5Mode;
    @SerializedName("p1p")
    private String period1Param;
    @SerializedName("p2p")
    private String period2Param;
    @SerializedName("p3p")
    private String period3Param;
    @SerializedName("p4p")
    private String period4Param;
    @SerializedName("p5p")
    private String period5Param;
    @SerializedName("p1d")
    private Date period1Date;
    @SerializedName("p2d")
    private Date period2Date;
    @SerializedName("p3d")
    private Date period3Date;
    @SerializedName("p4d")
    private Date period4Date;
    @SerializedName("p5d")
    private Date period5Date;

    @CheckForNull
    public Integer getId() {
        return id;
    }

    public Resource setId(@Nullable Integer id) {
        this.id = id;
        return this;
    }

    @CheckForNull
    public String getKey() {
        return key;
    }

    public Resource setKey(@Nullable String key) {
        this.key = key;
        return this;
    }

    @CheckForNull
    public String getDescription() {
        return description;
    }

    public Resource setDescription(@Nullable String description) {
        this.description = description;
        return this;
    }

    @CheckForNull
    public String getName() {
        return name;
    }

    @CheckForNull
    public String getName(boolean longFormatIfDefined) {
        if (longFormatIfDefined && longName != null && !"".equals(longName)) {
            return longName;
        }
        return name;
    }

    @CheckForNull
    public String getLongName() {
        return longName;
    }

    public Resource setLongName(@Nullable String longName) {
        this.longName = longName;
        return this;
    }

    public Resource setName(@Nullable String s) {
        this.name = s;
        return this;
    }

    @CheckForNull
    public String getScope() {
        return scope;
    }

    public Resource setScope(@Nullable String scope) {
        this.scope = scope;
        return this;
    }

    @CheckForNull
    public String getQualifier() {
        return qualifier;
    }

    public Resource setQualifier(@Nullable String qualifier) {
        this.qualifier = qualifier;
        return this;
    }

    @CheckForNull
    public String getLanguage() {
        return language;
    }

    public Resource setLanguage(@Nullable String language) {
        this.language = language;
        return this;
    }

    @CheckForNull
    public String getVersion() {
        return version;
    }

    public Resource setVersion(@Nullable String version) {
        this.version = version;
        return this;
    }

    @CheckForNull
    public Integer getCopy() {
        return copy;
    }

    public Resource setCopy(@Nullable Integer copy) {
        this.copy = copy;
        return this;
    }

    @CheckForNull
    public Date getDate() {
        return date;
    }

    public Resource setDate(@Nullable Date d) {
        this.date = d;
        return this;
    }

    @CheckForNull
    public Date getCreationDate() {
        return creationDate;
    }

    public Resource setCreationDate(@Nullable Date d) {
        this.creationDate = d;
        return this;
    }

    public List<Measure> getMsr() {
        if (msr == null) {
            return Collections.emptyList();
        }
        return msr;
    }

    public Measure getMeasure(String metricKey) {
        for (Measure measure : getMsr()) {
            if (metricKey.equals(measure.getKey())) {
                return measure;
            }
        }
        return null;
    }

    public Measure getMeasure(MetricKeys metricKey) {
        return getMeasure(metricKey.getKey());
    }

    public Double getMeasureValue(String metricKey) {
        Measure measure = getMeasure(metricKey);
        if (measure != null) {
            return measure.getValue();
        }
        return null;
    }

    public Integer getMeasureIntValue(String metricKey) {
        Double d = getMeasureValue(metricKey);
        if (d != null) {
            return d.intValue();
        }
        return null;
    }

    public String getMeasureFormattedValue(String metricKey, String defaultValue) {
        Measure measure = getMeasure(metricKey);
        if (measure != null) {
            return measure.getFormattedValue(defaultValue);
        }
        return defaultValue;
    }

    public void setMsr(List<Measure> msr) {
        this.msr = msr;
    }

    /**
     * @since 2.5 only on projects, else null
     */
    @CheckForNull
    public String getPeriod1Mode() {
        return period1Mode;
    }

    /**
     * @since 2.5
     */
    public Resource setPeriod1Mode(@Nullable String period1Mode) {
        this.period1Mode = period1Mode;
        return this;
    }

    /**
     * @since 2.5 only on projects, else null
     */
    @CheckForNull
    public String getPeriod2Mode() {
        return period2Mode;
    }

    /**
     * @since 2.5
     */
    public Resource setPeriod2Mode(@Nullable String period2Mode) {
        this.period2Mode = period2Mode;
        return this;
    }

    /**
     * @since 2.5 only on projects, else null
     */
    @CheckForNull
    public String getPeriod3Mode() {
        return period3Mode;
    }

    /**
     * @since 2.5
     */
    public Resource setPeriod3Mode(@Nullable String period3Mode) {
        this.period3Mode = period3Mode;
        return this;
    }

    /**
     * @since 2.5 only on projects, else null
     */
    @CheckForNull
    public String getPeriod4Mode() {
        return period4Mode;
    }

    /**
     * @since 2.5
     */
    public Resource setPeriod4Mode(@Nullable String period4Mode) {
        this.period4Mode = period4Mode;
        return this;
    }

    /**
     * @since 2.5 only on projects, else null
     */
    @CheckForNull
    public String getPeriod5Mode() {
        return period5Mode;
    }

    /**
     * @since 2.5
     */
    public Resource setPeriod5Mode(@Nullable String period5Mode) {
        this.period5Mode = period5Mode;
        return this;
    }

    /**
     * @since 2.5 only on projects, else null
     */
    @CheckForNull
    public String getPeriod1Param() {
        return period1Param;
    }

    /**
     * @since 2.5
     */
    public Resource setPeriod1Param(@Nullable String period1Param) {
        this.period1Param = period1Param;
        return this;
    }

    /**
     * @since 2.5 only on projects, else null
     */
    @CheckForNull
    public String getPeriod2Param() {
        return period2Param;
    }

    /**
     * @since 2.5
     */
    public Resource setPeriod2Param(@Nullable String period2Param) {
        this.period2Param = period2Param;
        return this;
    }

    /**
     * @since 2.5 only on projects, else null
     */
    @CheckForNull
    public String getPeriod3Param() {
        return period3Param;
    }

    /**
     * @since 2.5
     */
    public Resource setPeriod3Param(@Nullable String period3Param) {
        this.period3Param = period3Param;
        return this;
    }

    /**
     * @since 2.5 only on projects, else null
     */
    @CheckForNull
    public String getPeriod4Param() {
        return period4Param;
    }

    /**
     * @since 2.5
     */
    public Resource setPeriod4Param(@Nullable String period4Param) {
        this.period4Param = period4Param;
        return this;
    }

    /**
     * @since 2.5 only on projects, else null
     */
    @CheckForNull
    public String getPeriod5Param() {
        return period5Param;
    }

    /**
     * @since 2.5
     */
    public Resource setPeriod5Param(@Nullable String period5Param) {
        this.period5Param = period5Param;
        return this;
    }

    /**
     * @since 2.5 only on projects, else null
     */
    @CheckForNull
    public Date getPeriod1Date() {
        return period1Date;
    }

    /**
     * @since 2.5
     */
    public Resource setPeriod1Date(@Nullable Date period1Date) {
        this.period1Date = period1Date;
        return this;
    }

    /**
     * @since 2.5 only on projects, else null
     */
    @CheckForNull
    public Date getPeriod2Date() {
        return period2Date;
    }

    /**
     * @since 2.5
     */
    public Resource setPeriod2Date(@Nullable Date period2Date) {
        this.period2Date = period2Date;
        return this;
    }

    /**
     * @since 2.5 only on projects, else null
     */
    @CheckForNull
    public Date getPeriod3Date() {
        return period3Date;
    }

    /**
     * @since 2.5
     */
    public Resource setPeriod3Date(@Nullable Date period3Date) {
        this.period3Date = period3Date;
        return this;
    }

    /**
     * @since 2.5 only on projects, else null
     */
    @CheckForNull
    public Date getPeriod4Date() {
        return period4Date;
    }

    /**
     * @since 2.5
     */
    public Resource setPeriod4Date(@Nullable Date period4Date) {
        this.period4Date = period4Date;
        return this;
    }

    /**
     * @since 2.5 only on projects, else null
     */
    @CheckForNull
    public Date getPeriod5Date() {
        return period5Date;
    }

    /**
     * @since 2.5
     */
    public Resource setPeriod5Date(@Nullable Date period5Date) {
        this.period5Date = period5Date;
        return this;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[id=").append(id).append(",key=").append(key).append("]").toString();
    }
}
