package org.jenkinsci.plugins.exclusive.label;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.model.Label;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.queue.CauseOfBlockage;
import hudson.slaves.DumbSlave;
import java.lang.reflect.Field;
import org.jenkinsci.plugins.exclusive.label.ExclusiveLabels.NotExclusiveLabel;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class ExclusiveLabelsCanTakeTest {

    @Test
    void doesNotBlockWhenNodeHasNoExclusiveLabels(JenkinsRule r) throws Exception {
        ExclusiveLabels extension = configure(r, "exclusive");
        Node node = slave(r, "linux amd64");
        assertNull(extension.canTake(node, item(Label.get("linux"))));
    }

    @Test
    void blocksWhenItemRequestsNoLabel(JenkinsRule r) throws Exception {
        ExclusiveLabels extension = configure(r, "exclusive");
        Node node = slave(r, "exclusive linux");
        CauseOfBlockage cause = extension.canTake(node, item(null));
        assertInstanceOf(NotExclusiveLabel.class, cause);
        assertTrue(cause.getShortDescription().contains(node.getDisplayName()));
    }

    @Test
    void blocksWhenItemLabelOmitsExclusiveLabel(JenkinsRule r) throws Exception {
        ExclusiveLabels extension = configure(r, "exclusive");
        Node node = slave(r, "exclusive linux");
        CauseOfBlockage cause = extension.canTake(node, item(Label.get("linux")));
        assertInstanceOf(NotExclusiveLabel.class, cause);
    }

    @Test
    void allowsWhenItemLabelMentionsExclusiveLabel(JenkinsRule r) throws Exception {
        ExclusiveLabels extension = configure(r, "exclusive");
        Node node = slave(r, "exclusive linux");
        assertNull(extension.canTake(node, item(Label.get("linux && exclusive"))));
    }

    @Test
    void doesNotBlockWhenAssignedLabelExcludesNode(JenkinsRule r) throws Exception {
        ExclusiveLabels extension = configure(r, "exclusive");
        Node node = slave(r, "exclusive linux");
        assertNull(extension.canTake(node, item(Label.get("windows"))));
    }

    private static ExclusiveLabels configure(JenkinsRule r, String labels) {
        ExclusiveLabels extension =
                r.jenkins.getExtensionList(ExclusiveLabels.class).get(0);
        ((ExclusiveLabels.DescriptorImpl) extension.getDescriptor()).setLabelsInString(labels);
        return extension;
    }

    private static Node slave(JenkinsRule r, String labelString) throws Exception {
        DumbSlave slave = r.createOnlineSlave();
        slave.setLabelString(labelString);
        return slave;
    }

    private static Queue.BuildableItem item(Label assignedLabel) throws Exception {
        Queue.Task task = mock(Queue.Task.class);
        when(task.getAssignedLabel()).thenReturn(assignedLabel);
        Queue.BuildableItem item = mock(Queue.BuildableItem.class);
        // canTake() reads the public final Queue.Item.task field directly,
        // which a Mockito stub cannot intercept, so populate it reflectively.
        Field taskField = Queue.Item.class.getDeclaredField("task");
        taskField.setAccessible(true);
        taskField.set(item, task);
        return item;
    }
}
