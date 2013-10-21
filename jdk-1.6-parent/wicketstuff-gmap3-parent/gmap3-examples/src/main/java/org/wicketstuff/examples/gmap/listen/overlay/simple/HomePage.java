package org.wicketstuff.examples.gmap.listen.overlay.simple;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.wicketstuff.examples.gmap.WicketExamplePage;
import org.wicketstuff.gmap.GMap;
import org.wicketstuff.gmap.api.GEvent;
import org.wicketstuff.gmap.api.GEventHandler;
import org.wicketstuff.gmap.api.GLatLng;
import org.wicketstuff.gmap.api.GMarker;
import org.wicketstuff.gmap.api.GMarkerOptions;

/**
 * Demonstrates how to liste to markers.
 */
public class HomePage extends WicketExamplePage {

	private static final long serialVersionUID = 1L;

	public HomePage() {
		final GMap topMap = new GMap("topPanel");
		add(topMap);

		final GMarkerOptions options = new GMarkerOptions(topMap, topMap.getCenter()).draggable(true);
		final GMarker marker = new GMarker(options);
		final Label label = new Label("label", new PropertyModel<GLatLng>(marker, "latLng"));
		label.setOutputMarkupId(true);
		add(label);
		marker.addListener(GEvent.dragend, new GEventHandler() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onEvent(final AjaxRequestTarget target) {
				target.addComponent(label);
			}
		});
		topMap.addOverlay(marker);
	}
}
