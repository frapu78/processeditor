package com.inubit.research.validation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author tmi
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
            UnlabeledNodesCheckerTest.class,
            MessageFlowValidatorTest.class,
            EventBasedGatewayValidatorTest.class,
            BPMNValidatorTest.class,
            SequenceFlowValidatorTest.class,
            MessageEventValidatorTest.class,
            TaskValidatorTest.class,
            SubProcessValidatorTest.class,
            AssociationValidatorTest.class,
            CancelEventValidatorTest.class,
            InstantiationValidatorTest.class,
            NodeAdaptorTest.class,
            ClusterCotainmentAndAttachementValidatorTest.class,
            EventValidatorTest.class,
            LinkCorellationValidatorTest.class,
            MessageTextsReaderTest.class,
            ConversationAndConversationLinkValidatorTest.class,
            RootNodeTypeValidationTest.class,
            NonGatewayNodeValidatorTest.class,
            SoundnessCheckerTest.class
})
public class ValidationTestSuite {
}