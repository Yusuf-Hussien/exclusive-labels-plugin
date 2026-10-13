package org.jenkinsci.plugins.exclusive.label;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.Label;
import hudson.model.labels.LabelAtom;
import hudson.model.labels.LabelExpression;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ExclusiveLabelVisitorTest {

    private final ExclusiveLabelVisitor visitor = new ExclusiveLabelVisitor();

    private final LabelAtom a = new LabelAtom("a");
    private final LabelAtom b = new LabelAtom("b");

    @Test
    void atomIsTrueOnlyWhenPresent() {
        assertTrue(accepts(a, a));
        assertFalse(accepts(a, b));
    }

    @Test
    void parenDelegatesToItsBase() {
        assertTrue(accepts(new LabelExpression.Paren(a), a));
        assertFalse(accepts(new LabelExpression.Paren(a), b));
    }

    @Test
    void notAtomInvertsAtomMembership() {
        assertFalse(accepts(new LabelExpression.Not(a), a));
        assertTrue(accepts(new LabelExpression.Not(a), b));
    }

    @Test
    void notOfCompoundDelegatesThenInverts() {
        Label compound = new LabelExpression.And(a, b);
        assertFalse(accepts(new LabelExpression.Not(compound), a));
        assertTrue(accepts(new LabelExpression.Not(compound)));
    }

    @Test
    void andIsSatisfiedByEitherOperand() {
        Label and = new LabelExpression.And(a, b);
        assertTrue(accepts(and, a));
        assertTrue(accepts(and, b));
        assertFalse(accepts(and));
    }

    @Test
    void orIsSatisfiedByEitherOperand() {
        Label or = new LabelExpression.Or(a, b);
        assertTrue(accepts(or, b));
        assertFalse(accepts(or));
    }

    @Test
    void iffIsSatisfiedByEitherOperand() {
        Label iff = new LabelExpression.Iff(a, b);
        assertTrue(accepts(iff, a));
        assertFalse(accepts(iff));
    }

    @Test
    void impliesIsSatisfiedOnlyByItsRightOperand() {
        Label implies = new LabelExpression.Implies(a, b);
        assertTrue(accepts(implies, b));
        assertFalse(accepts(implies, a));
        assertFalse(accepts(implies));
    }

    private boolean accepts(Label expression, LabelAtom... present) {
        return expression.accept(visitor, Set.of(present));
    }
}
