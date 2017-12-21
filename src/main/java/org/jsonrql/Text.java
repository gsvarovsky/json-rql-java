package org.jsonrql;

public final class Text implements Value
{
    private final String text;

    public Text(String text)
    {
        this.text = text;
    }

    @Override
    public Object asJsonLd()
    {
        return text;
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public String toString()
    {
        return text;
    }
}
