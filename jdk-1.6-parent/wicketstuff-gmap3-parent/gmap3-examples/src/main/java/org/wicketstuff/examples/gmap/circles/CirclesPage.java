package org.wicketstuff.examples.gmap.circles;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.protocol.http.WebRequest;
import org.wicketstuff.examples.gmap.WicketExamplePage;
import org.wicketstuff.gmap.GMap;
import org.wicketstuff.gmap.api.GCircle;
import org.wicketstuff.gmap.api.GEvent;
import org.wicketstuff.gmap.api.GEventHandler;
import org.wicketstuff.gmap.api.GLatLng;

/**
 * Demonstrates the usage of circles.
 */
public class CirclesPage extends WicketExamplePage {
	public CirclesPage() {
		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);
		feedback.setOutputMarkupId(true);

		final GMap map = new GMap("topPanel");
		final GCircle circle = new GCircle(new GLatLng(37.35, -121.9), 10000, "#000000", 4, 0.7f, "#E9601A", 0.7f);
		circle.setEditable(true);
		circle.setDraggable(true);
		final GEventHandler eventHandler = new GEventHandler() {
			/** serialVersionUID. */
			private static final long serialVersionUID = 1L;

			@Override
			public void onEvent(final AjaxRequestTarget target) {
				final WebRequest request = (WebRequest) getRequest();
				final String radius = request.getParameter("overlay.radius");
				final String center = request.getParameter("overlay.latLng");
				feedback.info("Radius (in meters): " + radius);
				feedback.info("Coordinates: " + center);
				target.addComponent(feedback);
			}
		};
		circle.addListener(GEvent.radius_changed, eventHandler);
		circle.addListener(GEvent.center_changed, eventHandler);
		map.addOverlay(circle);
		map.setZoom(9);
		add(map);
	}
}
