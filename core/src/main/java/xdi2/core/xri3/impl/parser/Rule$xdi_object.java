/* -----------------------------------------------------------------------------
 * Rule$xdi_object.java
 * -----------------------------------------------------------------------------
 *
 * Producer : com.parse2.aparse.Parser 2.2
 * Produced : Fri Nov 16 12:53:05 CET 2012
 *
 * -----------------------------------------------------------------------------
 */

package xdi2.core.xri3.impl.parser;

import java.util.ArrayList;

final public class Rule$xdi_object extends Rule
{
  private Rule$xdi_object(String spelling, ArrayList<Rule> rules)
  {
    super(spelling, rules);
  }

  public Object accept(Visitor visitor)
  {
    return visitor.visit(this);
  }

  public static Rule$xdi_object parse(ParserContext context)
  {
    context.push("xdi-object");

    boolean parsed = true;
    int s0 = context.index;
    ArrayList<Rule> e0 = new ArrayList<Rule>();
    Rule rule;

    parsed = false;
    if (!parsed)
    {
      {
        ArrayList<Rule> e1 = new ArrayList<Rule>();
        int s1 = context.index;
        parsed = true;
        if (parsed)
        {
          boolean f1 = true;
          int c1 = 0;
          for (int i1 = 0; i1 < 1 && f1; i1++)
          {
            rule = Rule$xdi_segment.parse(context);
            if ((f1 = rule != null))
            {
              e1.add(rule);
              c1++;
            }
          }
          parsed = c1 == 1;
        }
        if (parsed)
          e0.addAll(e1);
        else
          context.index = s1;
      }
    }

    rule = null;
    if (parsed)
      rule = new Rule$xdi_object(context.text.substring(s0, context.index), e0);
    else
      context.index = s0;

    context.pop("xdi-object", parsed);

    return (Rule$xdi_object)rule;
  }
}

/* -----------------------------------------------------------------------------
 * eof
 * -----------------------------------------------------------------------------
 */
