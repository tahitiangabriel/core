package org.wicketstuff.examples.gmap.geocode;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.wicketstuff.examples.gmap.WicketExamplePage;
import org.wicketstuff.gmap.GMap;
import org.wicketstuff.gmap.api.GClientGeocoder;
import org.wicketstuff.gmap.api.GInfoWindow;
import org.wicketstuff.gmap.api.GLatLng;
import org.wicketstuff.gmap.api.GMapType;
import org.wicketstuff.gmap.geocoder.GeocoderStatus;

/**
 * Demonstrates geocoding client- and serverwise.
 */
public class HomePage extends WicketExamplePage {
	private static final long serialVersionUID = 1L;

	public HomePage() {
		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		feedback.setOutputMarkupId(true);
		add(feedback);

		final GMap bottomMap = new GMap("bottomPanel");
		bottomMap.setOutputMarkupId(true);
		bottomMap.setMapType(GMapType.SATELLITE);
		add(bottomMap);

		final Form<Object> geocodeForm = new Form<Object>("geocoder");
		add(geocodeForm);

		final TextField<String> addressTextField = new TextField<String>("address", new Model<String>(""));
		geocodeForm.add(addressTextField);

		final Button button = new Button("client");
		// Using GClientGeocoder the geocoding request
		// is performed on the client using JavaScript
		button.add(new GClientGeocoder("onclick", addressTextField, Duration.seconds(2)) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onGeoCode(final AjaxRequestTarget target, final GeocoderStatus status, final String address,
			        final GLatLng latLng) {
				if (status == GeocoderStatus.OK) {
					bottomMap.addOverlay(new GInfoWindow(latLng, "address: " + address));
					target.addComponent(feedback);
				} else {
					error("Unable to geocode (" + status + ")");
					target.addComponent(feedback);
				}
			}
		});
		geocodeForm.add(button);

		// Using ServerGeocoder the geocoding request
		// is performed on the server using Googles HTTP interface.
		// http://www.google.com/apis/maps/documentation/services.html#Geocoding_Direct
		geocodeForm.add(new AjaxButton("server", geocodeForm) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				try {
					final String address = addressTextField.getDefaultModelObjectAsString();
					final GLatLng latLng = new ServerGeocoder().findAddress(address);
					bottomMap.addOverlay(new GInfoWindow(latLng, "address: " + address));
					target.addComponent(feedback);
				} catch (final Exception e) {
					error("Unable to geocode (" + e.getMessage() + ")");
					target.addComponent(feedback);
				}
			}
		});
	}
}
