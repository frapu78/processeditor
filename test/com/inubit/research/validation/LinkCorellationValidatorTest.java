package com.inubit.research.validation;

import net.frapu.code.visualization.bpmn.ExclusiveGateway;
import net.frapu.code.visualization.bpmn.LinkIntermediateEvent;
import org.junit.Test;

/**
 *
 * @author tmi
 */
public class LinkCorellationValidatorTest extends BPMNValidationTestCommons {

    private LinkIntermediateEvent createLink(String linkName) {
        LinkIntermediateEvent link = new LinkIntermediateEvent();
        model.addNode(link);
        link.setText(linkName);
        return link;
    }

    private LinkIntermediateEvent createThrowingLink(String linkName) {
        LinkIntermediateEvent link = createLink(linkName);
        link.setProperty(LinkIntermediateEvent.PROP_EVENT_SUBTYPE,
                LinkIntermediateEvent.EVENT_SUBTYPE_THROWING);
        return link;
    }

    private LinkIntermediateEvent createCatchingLink(String linkName) {
        LinkIntermediateEvent link = createLink(linkName);
        link.setProperty(LinkIntermediateEvent.PROP_EVENT_SUBTYPE,
                LinkIntermediateEvent.EVENT_SUBTYPE_CATCHING);
        return link;
    }

    private ExclusiveGateway createExclusiveGateway() {
        ExclusiveGateway gateway = new ExclusiveGateway();
        model.addNode(gateway);
        return gateway;
    }

    @Test
    public void testTwoPoolsAndRootContainingCorrectLinks() {
        LinkIntermediateEvent link1Throw1InA = createThrowingLink("link1"),
                              link1Throw2InA = createThrowingLink("link1"),
                              link2ThrowInA = createThrowingLink("link2"),
                              link1CatchInA = createCatchingLink("link1"),
                              link2CatchInA = createCatchingLink("link2"),
                              link1ThrowInB = createThrowingLink("link1"),
                              link1CatchInB = createCatchingLink("link1"),
                              link1ThrowInRoot = createThrowingLink("link1"),
                              link1CatchInRoot = createCatchingLink("link1");
        getPoolA().addProcessNode(link1Throw1InA);
        getPoolA().addProcessNode(link1Throw2InA);
        getPoolA().addProcessNode(link2ThrowInA);
        getPoolA().addProcessNode(link1CatchInA);
        getPoolA().addProcessNode(link2CatchInA);
        getPoolB().addProcessNode(link1ThrowInB);
        getPoolB().addProcessNode(link1CatchInB);
        ExclusiveGateway splitGatewayInA = createExclusiveGateway(),
                         joinGatewayInA = createExclusiveGateway();
        getPoolA().addProcessNode(splitGatewayInA);
        getPoolA().addProcessNode(joinGatewayInA);
        addSequenceFlow(getTask1InA(true, false), splitGatewayInA);
        addSequenceFlow(splitGatewayInA, link1Throw1InA);
        addSequenceFlow(splitGatewayInA, link1Throw2InA);
        addSequenceFlow(splitGatewayInA, link2ThrowInA);
        addSequenceFlow(link1CatchInA, joinGatewayInA);
        addSequenceFlow(link2CatchInA, joinGatewayInA);
        addSequenceFlow(joinGatewayInA, getTask2InA(false, true));

        addSequenceFlow(getTask1InB(true, false), link1ThrowInB);
        addSequenceFlow(link1CatchInB, getTask2InB(false, true));

        addSequenceFlow(getNonPoolTask1(true, false), link1ThrowInRoot);
        addSequenceFlow(link1CatchInRoot, getNonPoolTask2(false, true));

        assertNoMessages(true);
    }

    @Test
    public void testThrowingLinkEventWithoutAnyCatchingLinkEvent() {
        LinkIntermediateEvent link = createThrowingLink("link0");
        ExclusiveGateway gateway = createExclusiveGateway();
        addSequenceFlow(getNonPoolTask1(true, false), gateway);
        addSequenceFlow(gateway, link);
        addSequenceFlow(gateway, getNonPoolTask2(false, true));
        assertOneError(texts.getLongText("noCatchingLink"),
                null, listOf(link), true);
    }

    @Test
    public void testThrowingLinkEventWithoutCorrelatingCatchingLinkEvent() {
        LinkIntermediateEvent link0 = createThrowingLink("link0"),
                              link1Throw = createThrowingLink("link1"),
                              link1Catch = createCatchingLink("link1");
        ExclusiveGateway gateway = createExclusiveGateway();
        addSequenceFlow(getNonPoolTask1(true, false), gateway);
        addSequenceFlow(gateway, link0);
        addSequenceFlow(gateway, link1Throw);
        addSequenceFlow(link1Catch, getGlobalEnd());
        assertOneError(texts.getLongText("noCatchingLink"),
                null, listOf(link0), true);
    }

    @Test
    public void testMultipleCatchingLinkEventsWithTheSameLabel() {
        LinkIntermediateEvent thrower = createThrowingLink("link0"),
                              catcher0 = createCatchingLink("link0"),
                              catcher1 = createCatchingLink("link0");
        addSequenceFlow(getNonPoolTask1(true, false), thrower);
        addSequenceFlow(catcher0, getGlobalEnd());
        addSequenceFlow(catcher1, gatewayBefore(getGlobalEnd()));
        assertOneError(texts.getLongText("multipleCatchingLinksForOneLabel"),
                listOf(catcher0, catcher1), true);
    }

    @Test
    public void testCatchingLinkEventWithoutThrowingPartner() {
        LinkIntermediateEvent link = createCatchingLink("link0");
        ExclusiveGateway gateway = createExclusiveGateway();
        addSequenceFlow(getNonPoolTask1(true, false), gateway);
        addSequenceFlow(link, gateway);
        addSequenceFlow(gateway, getNonPoolTask2(false, true));
        assertOneError(texts.getLongText("noThrowingLink"),
                null, listOf(link), true);
    }
}
