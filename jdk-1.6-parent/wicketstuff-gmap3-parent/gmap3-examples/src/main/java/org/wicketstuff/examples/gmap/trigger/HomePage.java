package org.wicketstuff.examples.gmap.trigger;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.wicketstuff.examples.gmap.WicketExamplePage;
import org.wicketstuff.gmap.GMap;

/**
 * Demonstrates how to trigger events on Map.
 */
public class HomePage extends WicketExamplePage {

	private static final long serialVersionUID = 1L;

	public HomePage() {
		final GMap map = new GMap("map");
		map.setOutputMarkupId(true);
		add(map);

		final WebMarkupContainer resize = new WebMarkupContainer("resize");
		resize.add(new AttributeModifier("onclick", true, Model.of(createResizeScript(map, 600, 600)
		        + map.getTriggerResizeScript().toString())));
		add(resize);

		final WebMarkupContainer resizeWrong = new WebMarkupContainer("resizeWrong");
		resizeWrong.add(new AttributeModifier("onclick", true, Model.of(createResizeScript(map, 600, 600))));
		add(resizeWrong);
	}

	private String createResizeScript(final GMap map, final int width, final int height) {
		return "getElementById('" + map.getMarkupId() + "').setAttribute('style','width:" + width + "px');"
		        + "getElementById('" + map.getMarkupId() + "').setAttribute('style','height:" + height + "px');";

	}
}
