package org.jenkinsci.plugins.exclusive.label;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class ExclusiveLabelsDescriptorTest {

    private static ExclusiveLabels.DescriptorImpl descriptor(JenkinsRule r) {
        return (ExclusiveLabels.DescriptorImpl)
                r.jenkins.getExtensionList(ExclusiveLabels.class).get(0).getDescriptor();
    }

    @Test
    void parsesSpaceSeparatedLabels(JenkinsRule r) {
        ExclusiveLabels.DescriptorImpl descriptor = descriptor(r);
        descriptor.setLabelsInString("exclusive1 exclusive2");
        assertEquals("exclusive1 exclusive2", descriptor.getLabelsInString());
        assertEquals(2, descriptor.getLabels().size());
        assertTrue(descriptor.getLabels().stream().anyMatch(l -> l.getName().equals("exclusive1")));
    }

    @Test
    void reportsNoLabelsWhenUnset(JenkinsRule r) {
        ExclusiveLabels.DescriptorImpl descriptor = descriptor(r);
        descriptor.setLabelsInString(null);
        assertTrue(descriptor.getLabels().isEmpty());
    }

    @Test
    void reportsNoLabelsWhenEmpty(JenkinsRule r) {
        ExclusiveLabels.DescriptorImpl descriptor = descriptor(r);
        descriptor.setLabelsInString("");
        assertTrue(descriptor.getLabels().isEmpty());
    }

    @Test
    void reportsDisplayName(JenkinsRule r) {
        assertEquals("Exclusive labels", descriptor(r).getDisplayName());
    }
}
