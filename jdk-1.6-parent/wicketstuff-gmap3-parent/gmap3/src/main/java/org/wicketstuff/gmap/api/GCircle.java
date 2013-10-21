/*
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.wicketstuff.gmap.api;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.wicketstuff.gmap.js.ObjectLiteral;

/**
 * Represents an Google Maps API's
 * <a href= "https://developers.google.com/maps/documentation/javascript/reference?#Circle">Circle</a>.
 */
public class GCircle extends GOverlay {
	private static final long serialVersionUID = 1L;

	/**
	 * The <a href="https://developers.google.com/maps/documentation/javascript/reference?#CircleOptions">options</a>
	 */
	private final GLatLng center;
	private final double radius;
	private final String strokeColor;
	private final int strokeWeight;
	private final float strokeOpacity;
	private final String fillColor;
	private final float fillOpacity;
	private boolean clickable = true;
	private boolean draggable;
	private boolean editable;
	private boolean visible = true;
	private Integer zIndex;

	public GCircle(final GLatLng center, final double radius, final String strokeColor, final int strokeWeight,
	        final float strokeOpacity, final String fillColor, final float fillOpacity) {
		if (center == null) {
			throw new IllegalArgumentException("center must not be null");
		}
		this.center = center;

		if (radius < 1 || radius > Double.MAX_VALUE) {
			throw new IllegalArgumentException("radius must be between 1 and " + Double.MAX_VALUE);
		}
		this.radius = radius;

		this.strokeColor = strokeColor;
		this.strokeWeight = strokeWeight;
		this.strokeOpacity = strokeOpacity;
		this.fillColor = fillColor;
		this.fillOpacity = fillOpacity;
	}

	@Override
	public String getJSconstructor() {
		return ("new google.maps.Circle(" + getSettings().toJS() + ")");
	}

	private ObjectLiteral getSettings() {
		final ObjectLiteral settings = new ObjectLiteral();

		settings.set("strokeWeight", String.valueOf(strokeWeight));
		settings.setString("strokeColor", strokeColor);
		settings.set("strokeOpacity", String.valueOf(strokeOpacity));
		settings.setString("fillColor", fillColor);
		settings.set("fillOpacity", String.valueOf(fillOpacity));
		settings.set("center", center.toString());
		settings.set("radius", String.valueOf(radius));

		if (!clickable) {
			settings.set("clickable", "false");
		}

		if (draggable) {
			settings.set("draggable", "true");
		}

		if (editable) {
			settings.set("editable", "true");
		}

		if (!visible) {
			settings.set("visible", "false");
		}

		if (zIndex != null) {
			settings.set("zIndex", String.valueOf(zIndex));
		}

		return settings;
	}

	@Override
	protected void updateOnAjaxCall(final AjaxRequestTarget target, final GEvent overlayEvent) {
		// empty method
	}

	public GCircle setClickable(final boolean clickable) {
		this.clickable = clickable;
		return this;
	}

	public GCircle setDraggable(final boolean draggable) {
		this.draggable = draggable;
		return this;
	}

	public GCircle setEditable(final boolean editable) {
		this.editable = editable;
		return this;
	}

	public GCircle setVisible(final boolean visible) {
		this.visible = visible;
		return this;
	}

	public GCircle setZIndex(final Integer zIndex) {
		if (zIndex != null) {
			if (zIndex < 0 || zIndex > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("zindex must be between 0 and " + Integer.MAX_VALUE);
			}
		}
		this.zIndex = zIndex;
		return this;
	}
}
