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
package org.wicketstuff.gmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.gmap.api.GEvent;
import org.wicketstuff.gmap.api.GLatLng;
import org.wicketstuff.gmap.api.GLatLngBounds;
import org.wicketstuff.gmap.api.GMapType;
import org.wicketstuff.gmap.api.GMarker;
import org.wicketstuff.gmap.api.GMarkerOptions;
import org.wicketstuff.gmap.api.GOverlay;
import org.wicketstuff.gmap.event.GEventListenerBehavior;

/**
 * Wicket component to embed <a href="http://maps.google.com">Google Maps</a> into your pages.
 * <p>
 */
public class GMap extends Panel implements GOverlayContainer {

	/** log. */
	private static final Logger log = LoggerFactory.getLogger(GMap.class);
	private static final long serialVersionUID = 1L;
	// Center is Palo Alto
	private GLatLng center = new GLatLng(37.4419, -122.1419);
	private boolean draggingEnabled = true;
	private boolean doubleClickZoomEnabled = false;
	private boolean scrollWheelZoomEnabled = false;
	private boolean streetViewControlEnabled = false;
	private boolean zoomControlEnabled = true;
	private boolean mapTypeControlEnabled = true;
	private boolean scaleControlEnabled = false;
	private boolean panControlEnabled = true;
	private GMapType mapType = GMapType.ROADMAP;
	private int zoom = 13;
	private final Map<String, GOverlay> overlays = new HashMap<String, GOverlay>();
	private final WebMarkupContainer map;
	private GLatLngBounds bounds;
	private OverlayListener overlayListener = null;

	/**
	 * If set to true map loading will not produce any JavaScript errors in case
	 * google maps API cannot be found (e.g. no Internet connection)
	 */
	private boolean failSilently = false;

	/**
	 * Construct.
	 * 
	 * Default the header contributor of the component will added and the gmap will be inited directly on rendering of
	 * the map.
	 * 
	 * @param id wicket id
	 */
	public GMap(final String id) {
		this(id, new GMapHeaderContributor());
	}

	public GMap(final String id, final boolean sensor) {
		this(id, new GMapHeaderContributor(sensor));
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param headerContrib
	 */
	public GMap(final String id, final GMapHeaderContributor headerContrib) {
		super(id);

		if (headerContrib != null) {
			add(headerContrib);
		}

		map = new WebMarkupContainer("map");
		map.setOutputMarkupId(true);
		add(map);

		overlayListener = new OverlayListener();
		add(overlayListener);

		add(new HeaderContributor(new IHeaderContributor() {
			private static final long serialVersionUID = 1L;

			/**
			 * @see org.apache.wicket.markup.html.IHeaderContributor#renderHead(org.apache.wicket.markup.html.IHeaderResponse)
			 */
			@Override
			public void renderHead(final IHeaderResponse response) {
				response.renderOnDomReadyJavascript(getJSinit());
			}
		}));

	}

	/**
	 * @return the markup-id of the container
	 */
	public String getMapId() {
		return map.getMarkupId();
	}

	@Override
	protected void onBeforeRender() {
		super.onBeforeRender();

		if (Application.DEVELOPMENT.equalsIgnoreCase(Application.get().getConfigurationType())
		        && !Application.get().getMarkupSettings().getStripWicketTags()) {
			log.warn("Application is in DEVELOPMENT mode && Wicket tags are not stripped,"
			        + "Some Chrome Versions will not render the GMap."
			        + " Change to DEPLOYMENT mode  || turn on Wicket tags stripping." + " See:"
			        + " http://www.nabble.com/Gmap2-problem-with-Firefox-3.0-to18137475.html.");
		}
	}

	/**
	 * Add an overlay.
	 * 
	 * @see GOverlayContainer#addOverlay(GOverlay)
	 * @param overlay
	 *            overlay to add
	 * @return This
	 */
	@Override
	public GMap addOverlay(final GOverlay overlay) {
		overlays.put(overlay.getId(), overlay);
		overlay.setParent(this);

		final AjaxRequestTarget target = AjaxRequestTarget.get();
		if (target != null && findPage() != null) {
			target.appendJavascript(overlay.getJS());
		}

		return this;
	}

	/**
	 * Remove an overlay.
	 * 
	 * @see GOverlayContainer#removeOverlay(GOverlay)
	 * @param overlay
	 *            overlay to remove
	 * @return This
	 */
	@Override
	public GMap removeOverlay(final GOverlay overlay) {
		while (overlays.containsKey(overlay.getId())) {
			overlays.remove(overlay.getId());
		}

		final AjaxRequestTarget target = AjaxRequestTarget.get();
		if (target != null && findPage() != null) {
			target.appendJavascript(overlay.getJSremove());
		}

		overlay.setParent(null);

		return this;
	}

	/**
	 * Clear all overlays.
	 * 
	 * @see GOverlayContainer#removeAllOverlays()
	 * @return This
	 */
	@Override
	public GMap removeAllOverlays() {
		for (final GOverlay overlay : overlays.values()) {
			overlay.setParent(null);
		}
		overlays.clear();
		final AjaxRequestTarget target = AjaxRequestTarget.get();
		if (target != null && findPage() != null) {
			target.appendJavascript(getJSinvoke("clearOverlays()"));
		}
		return this;
	}

	/**
	 * @see GOverlayContainer#getOverlays()
	 */
	@Override
	public List<GOverlay> getOverlays() {
		return Collections.unmodifiableList(new ArrayList<GOverlay>(overlays.values()));
	}

	public GLatLngBounds getBounds() {
		return bounds;
	}

	/**
	 * Returns the script for triggering an event on map.
	 * 
	 * @param event
	 * @return
	 */
	public CharSequence getTriggerEventScript(final String event) {
		return "Wicket.maps['" + getMapId() + "'].triggerEvent('" + event + "')";
	}

	/**
	 * @return returns the script to make map re-paint after resize.
	 */
	public CharSequence getTriggerResizeScript() {
		return "Wicket.maps['" + getMapId() + "'].triggerResize();";
	}

	/**
	 * Sets if dragging should be allowed or not.
	 * 
	 * @param enabled true if dragging should be allowed, false otherwise
	 */
	public void setDraggingEnabled(final boolean enabled) {
		if (this.draggingEnabled != enabled) {
			draggingEnabled = enabled;

			final AjaxRequestTarget target = AjaxRequestTarget.get();
			if (target != null && findPage() != null) {
				target.appendJavascript(getJSsetDraggingEnabled(enabled));
			}
		}
	}

	/**
	 * Is dragging allowed? Enabled by default.
	 * 
	 * @return true if it's allowed, false if not
	 */
	public boolean isDraggingEnabled() {
		return draggingEnabled;
	}

	/**
	 * Sets if zooming-by-doubleclicking should be allowed or not.
	 * 
	 * @param enabled true if zooming-by-doubleclicking should be allowed, false otherwise
	 */
	public void setDoubleClickZoomEnabled(final boolean enabled) {
		if (this.doubleClickZoomEnabled != enabled) {
			doubleClickZoomEnabled = enabled;

			final AjaxRequestTarget target = AjaxRequestTarget.get();
			if (target != null && findPage() != null) {
				target.appendJavascript(getJSsetDoubleClickZoomEnabled(enabled));
			}
		}
	}

	/**
	 * Is the function zooming-by-doubleclick enabled?
	 * Disabled by default.
	 * 
	 * @return true if enabled, false if disabled
	 */
	public boolean isDoubleClickZoomEnabled() {
		return doubleClickZoomEnabled;
	}

	/**
	 * Sets if zooming-by-mousewheel should be allowed or not.
	 * 
	 * @param enabled true if zooming-by-mousewheel should be allowed, false otherwise
	 */
	public void setScrollWheelZoomEnabled(final boolean enabled) {
		if (this.scrollWheelZoomEnabled != enabled) {
			scrollWheelZoomEnabled = enabled;

			final AjaxRequestTarget target = AjaxRequestTarget.get();
			if (target != null && findPage() != null) {
				target.appendJavascript(getJSsetScrollWheelZoomEnabled(enabled));
			}
		}
	}

	/**
	 * Is the function zooming-by-mousewheel enabled?
	 * Disabled by default.
	 * 
	 * @return true if enabled, false if disabled
	 */
	public boolean isScrollWheelZoomEnabled() {
		return scrollWheelZoomEnabled;
	}

	/**
	 * Is the StreetView control enabled?
	 * Disabled by default.
	 * 
	 * @return true if enabled, false if disabled
	 */
	public boolean isStreetViewControlEnabled() {
		return streetViewControlEnabled;
	}

	/**
	 * Sets if the StreeView control should be visible or not.
	 * 
	 * @param enabled true if StreetView should be allowed, false otherwise
	 */
	public void setStreetViewControlEnabled(final boolean enabled) {
		if (this.streetViewControlEnabled != enabled) {
			streetViewControlEnabled = enabled;

			final AjaxRequestTarget target = AjaxRequestTarget.get();
			if (target != null && findPage() != null) {
				target.appendJavascript(getJSsetStreetViewControlEnabled(enabled));
			}
		}
	}

	/**
	 * Is the zoom control enabled?
	 * Enabled by default.
	 * 
	 * @return true if enabled, false if disabled
	 */
	public boolean isZoomControlEnabled() {
		return zoomControlEnabled;
	}

	/**
	 * Sets if the zoom control should be visible or not.
	 * 
	 * @param enabled true if the zoom-control should be enabled, false otherwise
	 */
	public void setZoomControlEnabled(final boolean enabled) {
		if (this.zoomControlEnabled != enabled) {
			this.zoomControlEnabled = enabled;

			final AjaxRequestTarget target = AjaxRequestTarget.get();
			if (target != null && findPage() != null) {
				target.appendJavascript(getJSsetZoomControlEnabled(enabled));
			}
		}
	}

	/**
	 * Is the map type control enabled?
	 * Enabled by default.
	 * 
	 * @return true if enabled, false if disabled
	 */
	public boolean isMapTypeControlEnabled() {
		return mapTypeControlEnabled;
	}

	/**
	 * Sets if the map type control should be visible or not.
	 * 
	 * @param enabled true if you want the user to have the possibility to
	 *            change the map type, false otherwise
	 */
	public void setMapTypeControlEnabled(final boolean enabled) {

		if (this.mapTypeControlEnabled != enabled) {
			this.mapTypeControlEnabled = enabled;

			final AjaxRequestTarget target = AjaxRequestTarget.get();
			if (target != null && findPage() != null) {
				target.appendJavascript(getJSsetMapTypeControlEnabled(enabled));
			}
		}
	}

	/**
	 * Is the scale control enabled?
	 * Disabled by default.
	 * 
	 * @return true if enabled, false if disabled
	 */
	public boolean isScaleControlEnabled() {
		return scaleControlEnabled;
	}

	/**
	 * Sets if the scale control should be visible or not.
	 * 
	 * @param enabled true if the scale-control should be enabled, false otherwise
	 */
	public void setScaleControlEnabled(final boolean enabled) {
		if (this.scaleControlEnabled != enabled) {
			this.scaleControlEnabled = enabled;

			final AjaxRequestTarget target = AjaxRequestTarget.get();
			if (target != null && findPage() != null) {
				target.appendJavascript(getJSsetScaleControlEnabled(enabled));
			}
		}
	}

	/**
	 * Is the pan control enabled?
	 * Enabled by default.
	 * 
	 * @return true if enabled, false if disabled
	 */
	public boolean isPanControlEnabled() {
		return panControlEnabled;
	}

	/**
	 * Sets if the pan control should be visible or not.
	 * 
	 * @param enabled true if the pan-control should be enabled, false otherwise
	 */
	public void setPanControlEnabled(final boolean enabled) {
		if (this.panControlEnabled != enabled) {
			this.panControlEnabled = enabled;

			final AjaxRequestTarget target = AjaxRequestTarget.get();
			if (target != null && findPage() != null) {
				target.appendJavascript(getJSsetPanControlEnabled(enabled));
			}
		}
	}

	/**
	 * @return the current map type.
	 * @see GMapType
	 */
	public GMapType getMapType() {
		return mapType;
	}

	/**
	 * Sets the map type which should be used.
	 * 
	 * @param mapType the map type
	 * @see GMapType
	 */
	public void setMapType(final GMapType mapType) {
		if (this.mapType != mapType) {
			this.mapType = mapType;

			final AjaxRequestTarget target = AjaxRequestTarget.get();
			if (target != null && findPage() != null) {
				target.appendJavascript(mapType.getJSsetMapType(GMap.this));
			}
		}
	}

	/**
	 * @return the current zoom level
	 */
	public int getZoom() {
		return zoom;
	}

	/**
	 * Sets a new zoom level.
	 * 
	 * @param level the new zoom level
	 */
	public void setZoom(final int level) {
		if (this.zoom != level) {
			this.zoom = level;

			final AjaxRequestTarget target = AjaxRequestTarget.get();
			if (target != null && findPage() != null) {
				target.appendJavascript(getJSsetZoom(zoom));
			}
		}
	}

	/**
	 * @return the current center point
	 */
	public GLatLng getCenter() {
		return center;
	}

	/**
	 * Set the center.
	 * 
	 * @param center
	 *            center to set
	 */
	public void setCenter(final GLatLng center) {
		if (!this.center.equals(center)) {
			this.center = center;

			final AjaxRequestTarget target = AjaxRequestTarget.get();
			if (target != null && findPage() != null) {
				target.appendJavascript(getJSsetCenter(center));
			}
		}
	}

	/**
	 * Changes the center point of the map to the given point. If the point is already visible in the current map view,
	 * change the center in a smooth animation.
	 * 
	 * @param center
	 *            the new center of the map
	 */
	public void panTo(final GLatLng center) {
		if (!this.center.equals(center)) {
			this.center = center;

			final AjaxRequestTarget target = AjaxRequestTarget.get();
			if (target != null && findPage() != null) {
				target.appendJavascript(getJSpanTo(center));
			}
		}
	}

	/**
	 * Generates the JavaScript used to instantiate this GMap3 as an JavaScript class on the client side.
	 * 
	 * @return The generated JavaScript
	 */
	public String getJSinit() {
		final StringBuilder js = new StringBuilder("new WicketMap('" + getMapId() + "', " + isFailSilently() + ");\n");

		js.append(getJSinvoke("clearOverlays()"));
		js.append(overlayListener.getJSinit());
		js.append(getJSsetCenter(getCenter()));
		js.append(getJSsetZoom(getZoom()));
		js.append(getJSsetDraggingEnabled(draggingEnabled));
		js.append(getJSsetDoubleClickZoomEnabled(doubleClickZoomEnabled));
		js.append(getJSsetScrollWheelZoomEnabled(scrollWheelZoomEnabled));
		js.append(getJSsetStreetViewControlEnabled(streetViewControlEnabled));
		js.append(getJSsetZoomControlEnabled(zoomControlEnabled));
		js.append(getJSsetScaleControlEnabled(scaleControlEnabled));
		js.append(getJSsetMapTypeControlEnabled(mapTypeControlEnabled));
		js.append(getJSsetPanControlEnabled(panControlEnabled));

		js.append(mapType.getJSsetMapType(this));

		// Add the overlays.
		for (final GOverlay overlay : overlays.values()) {
			js.append(overlay.getJS());
		}
		for (final Object behavior : getBehaviors(GEventListenerBehavior.class)) {
			js.append(((GEventListenerBehavior) behavior).getJSaddListener());
		}

		return js.toString();
	}

	/**
	 * Convenience method for generating a JavaScript call on this GMap with the given invocation.
	 * 
	 * @param invocation
	 *            The JavaScript call to invoke on this GMap.
	 * @return The generated JavaScript.
	 */
	public String getJSinvoke(final String invocation) {
		return getJsReference() + "." + invocation + ";\n";
	}

	/**
	 * Build a reference in JS-Scope.
	 */
	public String getJsReference() {
		return "Wicket.maps['" + getMapId() + "']";
	}

	/**
	 * @see #fitMarkers(List, boolean, double)
	 */
	public void fitMarkers(final List<GLatLng> markersToShow) {
		fitMarkers(markersToShow, false, 0.0);
	}

	/**
	 * @see #fitMarkers(List, boolean, double)
	 */
	public void fitMarkers(final List<GLatLng> markersToShow, final boolean showMarkersForPoints) {
		fitMarkers(markersToShow, showMarkersForPoints, 0.0);
	}

	/**
	 * <p>
	 * Makes the map zoom out and centre around all the GLatLng points in markersToShow.
	 * <p>
	 * Big ups to Doug Leeper for the code.
	 * 
	 * @see <a href= "http://www.nabble.com/Re%3A-initial-GMap2-bounds-question-p19886673.html" >Doug's Nabble post</a>
	 * @param markersToShow
	 *            the points to centre around.
	 * @param showMarkersForPoints
	 *            if true, will also add basic markers to the map for each point focused on. Just a simple convenience
	 *            method - you will probably want to turn this off so that you can show more information with each
	 *            marker when clicked etc.
	 */
	public void fitMarkers(final List<GLatLng> markersToShow, final boolean showMarkersForPoints,
	        final double zoomAdjustment) {
		if (markersToShow.isEmpty()) {
			log.warn("Empty list provided to GMap.fitMarkers method.");
			return;
		}

		final AjaxRequestTarget target = AjaxRequestTarget.get();
		if (target != null && findPage() != null) {
			final StringBuilder buf = new StringBuilder();
			buf.append("var bounds = new google.maps.LatLngBounds();\n");
			buf.append("var map = " + GMap.this.getJSinvoke("map") + ";\n");

			// Ask google maps to keep extending the bounds to include each
			// point
			for (final GLatLng point : markersToShow) {
				buf.append("bounds.extend( " + point.getJSconstructor() + " );\n");
			}

			buf.append("map.fitBounds(bounds);\n");
			buf.append("map.panToBounds(bounds);\n");

			target.appendJavascript(buf.toString());
		}

		// show the markers
		if (showMarkersForPoints) {
			for (final GLatLng location : markersToShow) {
				this.addOverlay(new GMarker(new GMarkerOptions(this, location)));
			}
		}
	}

	private String getJSsetDraggingEnabled(final boolean enabled) {
		return getJSinvoke("setDraggingEnabled(" + enabled + ")");
	}

	private String getJSsetDoubleClickZoomEnabled(final boolean enabled) {
		return getJSinvoke("setDoubleClickZoomEnabled(" + enabled + ")");
	}

	private String getJSsetScrollWheelZoomEnabled(final boolean enabled) {
		return getJSinvoke("setScrollWheelZoomEnabled(" + enabled + ")");
	}

	private String getJSsetStreetViewControlEnabled(final boolean enabled) {
		return getJSinvoke("setStreetViewControlEnabled(" + enabled + ")");
	}

	private String getJSsetZoomControlEnabled(final boolean enabled) {
		return getJSinvoke("setZoomControlEnabled(" + enabled + ")");
	}

	private String getJSsetScaleControlEnabled(final boolean enabled) {
		return getJSinvoke("setScaleControlEnabled(" + enabled + ")");
	}

	private String getJSsetMapTypeControlEnabled(final boolean enabled) {
		return getJSinvoke("setMapTypeControlEnabled(" + enabled + ")");
	}

	private String getJSsetPanControlEnabled(final boolean enabled) {
		return getJSinvoke("setPanControlEnabled(" + enabled + ")");
	}

	private String getJSsetZoom(final int zoom) {
		return getJSinvoke("setZoom(" + zoom + ")");
	}

	private String getJSsetCenter(final GLatLng center) {
		if (center != null) {
			if (!failSilently) {
				return getJSinvoke("setCenter(" + center.getJSconstructor() + ")");
			} else {
				return getJSinvoke("setCenterFailSafe(" + center.getArguments() + ")");
			}
		}
		return "";
	}

	private String getJSpanDirection(final int dx, final int dy) {
		return getJSinvoke("panDirection(" + dx + "," + dy + ")");
	}

	private String getJSpanTo(final GLatLng center) {
		return getJSinvoke("panTo(" + center.getJSconstructor() + ")");
	}

	private String getJSzoomOut() {
		return getJSinvoke("zoomOut()");
	}

	private String getJSzoomIn() {
		return getJSinvoke("zoomIn()");
	}

	/**
	 * Update state from a request to an AJAX target.
	 * You need to call this method explictly if you want to have up-to-date values.
	 */
	public void update() {
		// Attention: don't use setters as this will result in an endless
		// AJAX request loop
		final Request request = RequestCycle.get().getRequest();
		if (request.getParameter("bounds") != null) {
			bounds = GLatLngBounds.parse(request.getParameter("bounds"));
		}
		if (request.getParameter("center") != null) {
			center = GLatLng.parse(request.getParameter("center"));
		}
		if (request.getParameter("zoom") != null) {
			zoom = Integer.parseInt(request.getParameter("zoom"));
		}
		final String requestMapType = request.getParameter("currentMapType");
		mapType = requestMapType != null ? GMapType.valueOf(request.getParameter("currentMapType")) : mapType;
	}

	public void setOverlays(final List<GOverlay> overlays) {
		removeAllOverlays();
		for (final GOverlay overlay : overlays) {
			addOverlay(overlay);
		}
	}

	private static abstract class JSMethodBehavior extends AbstractBehavior {
		private static final long serialVersionUID = 1L;
		private final String attribute;

		public JSMethodBehavior(final String attribute) {
			this.attribute = attribute;
		}

		@Override
		public void onComponentTag(final Component component, final ComponentTag tag) {
			String invoke = getJSinvoke();

			if (attribute.equalsIgnoreCase("href")) {
				invoke = "javascript:" + invoke;
			}

			tag.put(attribute, invoke);
		}

		protected abstract String getJSinvoke();
	}

	public class ZoomOutBehavior extends JSMethodBehavior {
		private static final long serialVersionUID = 1L;

		public ZoomOutBehavior(final String event) {
			super(event);
		}

		@Override
		protected String getJSinvoke() {
			return getJSzoomOut();
		}
	}

	public class ZoomInBehavior extends JSMethodBehavior {
		private static final long serialVersionUID = 1L;

		public ZoomInBehavior(final String event) {
			super(event);
		}

		@Override
		protected String getJSinvoke() {
			return getJSzoomIn();
		}
	}

	public class PanDirectionBehavior extends JSMethodBehavior {
		private static final long serialVersionUID = 1L;
		private final int dx;
		private final int dy;

		public PanDirectionBehavior(final String event, final int dx, final int dy) {
			super(event);
			this.dx = dx;
			this.dy = dy;
		}

		@Override
		protected String getJSinvoke() {
			return getJSpanDirection(dx, dy);
		}
	}

	public class SetZoomBehavior extends JSMethodBehavior {
		private static final long serialVersionUID = 1L;
		private final int zoomBehavior;

		public SetZoomBehavior(final String event, final int zoom) {
			super(event);
			zoomBehavior = zoom;
		}

		@Override
		protected String getJSinvoke() {
			return getJSsetZoom(zoomBehavior);
		}
	}

	public class SetCenterBehavior extends JSMethodBehavior {
		private static final long serialVersionUID = 1L;
		private final GLatLng gLatLng;

		public SetCenterBehavior(final String event, final GLatLng gLatLng) {
			super(event);
			this.gLatLng = gLatLng;
		}

		@Override
		protected String getJSinvoke() {
			return getJSsetCenter(gLatLng);
		}
	}

	public class SetMapTypeBehavior extends JSMethodBehavior {
		private static final long serialVersionUID = 1L;
		private final GMapType mapTypeBehavior;

		public SetMapTypeBehavior(final String event, final GMapType mapType) {
			super(event);
			mapTypeBehavior = mapType;
		}

		@Override
		protected String getJSinvoke() {
			return mapTypeBehavior.getJSsetMapType(GMap.this);
		}
	}

	public class OverlayListener extends AbstractDefaultAjaxBehavior {
		private static final long serialVersionUID = 1L;

		@Override
		protected void respond(final AjaxRequestTarget target) {
			final Request request = RequestCycle.get().getRequest();

			final String overlayId = request.getParameter("overlay.overlayId").replace("overlay", "");
			final String event = request.getParameter("overlay.event");
			final GOverlay overlay = overlays.get(overlayId);
			if (overlay != null) {
				overlay.onEvent(target, GEvent.valueOf(event));
			}
		}

		public Object getJSinit() {
			return GMap.this.getJSinvoke("overlayListenerCallbackUrl = '" + this.getCallbackUrl() + "'");

		}
	}

	public boolean isFailSilently() {
		return failSilently;
	}

	public void setFailSilently(final boolean failSilently) {
		this.failSilently = failSilently;
	}
}
