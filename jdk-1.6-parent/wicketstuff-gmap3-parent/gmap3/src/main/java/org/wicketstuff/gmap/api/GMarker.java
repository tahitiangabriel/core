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

import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.wicketstuff.gmap.js.Constructor;

/**
 * Represents an Google Maps API's <a href="http://www.google.com/apis/maps/documentation/reference.html#GMarker"
 * >GMarker</a>.
 */
public class GMarker extends GOverlay {

	private static final long serialVersionUID = 1L;
	private final GMarkerOptions options;

	public GMarker(final GMarkerOptions options) {
		super();
		this.options = options;
	}

	public GLatLng getLatLng() {
		return options.getLatLng();
	}

	public GMarkerOptions getMarkerOptions() {
		return options;
	}

	/**
	 * @see GOverlay#getJSconstructor()
	 */
	@Override
	public String getJSconstructor() {
		final Constructor constructor = new Constructor("google.maps.Marker").add(options.getJSconstructor());
		return constructor.toJS();
	}

	/**
	 * @see GOverlay#updateOnAjaxCall(org.apache.wicket.ajax.AjaxRequestTarget, GEvent)
	 */
	@Override
	protected void updateOnAjaxCall(final AjaxRequestTarget target, final GEvent overlayEvent) {
		final Request request = RequestCycle.get().getRequest();
		options.setLatLng(GLatLng.parse(request.getParameter("overlay.latLng")));
	}
}
