package org.wicketstuff.examples.gmap.simple.ajax;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.wicketstuff.examples.gmap.WicketExamplePage;
import org.wicketstuff.gmap.GMapHeaderContributor;

/**
 * SimplePage for the wicket-contrib-gmap3 project
 */
public class SimplePage extends WicketExamplePage {

	public SimplePage() {
		add(new GMapHeaderContributor());
		final MapPanel p = new MapPanel("map");
		p.setVisible(false);
		p.setOutputMarkupId(true);
		p.setOutputMarkupPlaceholderTag(true);

		add(p);
		add(new AjaxLink<Void>("show") {
			/** serialVersionUID. */
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				p.setVisible(true);
				target.addComponent(p);
			}
		});

		add(new AjaxLink<Void>("hide") {
			/** serialVersionUID. */
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				p.setVisible(false);
				target.addComponent(p);
			}
		});
	}
}
