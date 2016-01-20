package org.wicketstuff.datatables.options;

import de.agilecoders.wicket.jquery.util.Json;

import java.io.Serializable;

/**
 * The definition of a column.
 *
 * @see <a href="https://datatables.net/reference/option/columns">DataTables columns</a>
 */
public class Column implements Serializable {

    private final String data;
    private final boolean orderable;
    private Json.RawValue render;

    public Column(final String data) {
        this(data, true);
    }

    public Column(final String data, final boolean orderable) {
        this.data = data;
        this.orderable = orderable;
    }

    public String getData() {
        return data;
    }

    public boolean isOrderable() {
        return orderable;
    }

    public Json.RawValue getRender() {
        return render;
    }

    public void setRender(final Json.RawValue renderFunction) {
        this.render = renderFunction;
    }
}
