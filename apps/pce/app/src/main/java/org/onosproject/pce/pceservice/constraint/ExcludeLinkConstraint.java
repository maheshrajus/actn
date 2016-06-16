/*
 * Copyright 2016-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.pce.pceservice.constraint;

import org.onosproject.net.Link;
import org.onosproject.net.intent.ResourceContext;
import org.onosproject.net.intent.constraint.BooleanConstraint;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Constraint that evaluates whether links satisfies excluding the link request.
 */
public final class ExcludeLinkConstraint extends BooleanConstraint {

    private final List<Link> links;

    // Constructor for serialization
    private ExcludeLinkConstraint() {
        links = null;

    }

    /**
     * Creates a new exclude link constraint.
     *
     * @param links exclude links
     */
    public ExcludeLinkConstraint(List<Link> links) {
        this.links = links;
    }

    /**
     * Creates a new exclude link constraint.
     *
     * @param links exclude links
     */
    public static ExcludeLinkConstraint of(List<Link> links) {
        return new ExcludeLinkConstraint(links);
    }

    /**
     * Obtains exclude links.
     *
     * @return exclude links
     */
    public List<Link> links() {
        return links;
    }

    @Override
    public boolean isValid(Link link, ResourceContext context) {
        return !links.contains(link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(links);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof ExcludeLinkConstraint) {
            ExcludeLinkConstraint other = (ExcludeLinkConstraint) obj;
            return Objects.equals(this.links, other.links);
        }

        return false;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("links", links)
                .toString();
    }
}