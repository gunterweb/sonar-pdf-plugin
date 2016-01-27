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

import java.io.Serializable;
import java.util.Map;

import org.sonarqube.ws.client.unmarshallers.JsonUtils;

/**
 * Paging model
 *
 */
public class Paging implements Model {

    /**
     * 
     */
    private static final long serialVersionUID = -2540275275018566305L;
    private final Map<Serializable, Serializable> json;

    /**
     * For internal use
     */
    public Paging(Map<Serializable, Serializable> json) {
        this.json = json;
    }

    public Integer pageSize() {
        return JsonUtils.getInteger(json, "ps");
    }

    public Integer pageIndex() {
        return JsonUtils.getInteger(json, "p");
    }

    public Integer total() {
        return JsonUtils.getInteger(json, "total");
    }

    public Integer pages() {
        return JsonUtils.getInteger(json, "pages");
    }

}