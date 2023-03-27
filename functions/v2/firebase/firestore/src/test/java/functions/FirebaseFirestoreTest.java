/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package functions;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.TestLogHandler;
import com.google.events.cloud.firestore.v1.Document;
import com.google.events.cloud.firestore.v1.DocumentEventData;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import java.net.URI;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FirebaseFirestoreTest {

  // Loggers + handlers for various tested classes
  // (Must be declared at class-level, or LoggingHandler won't detect log
  // records!)
  private static final Logger logger = Logger.getLogger(FirebaseFirestore.class.getName());

  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  @BeforeClass
  public static void beforeClass() {
    logger.addHandler(LOG_HANDLER);
  }

  @After
  public void afterTest() {
    LOG_HANDLER.clear();
  }

  @Test
  public void functionsFirebaseFirestore_shouldUnmarshalAndPrint()
      throws InvalidProtocolBufferException {
    Document oldValue = Document.newBuilder()
        .setName("oldValue")
        .build();
    Document newValue = Document.newBuilder()
        .setName("newValue")
        .build();
    DocumentEventData firestorePayload = DocumentEventData.newBuilder()
        .setValue(newValue)
        .setOldValue(oldValue)
        .build();
    Any anyPayload = Any.newBuilder()
        .setTypeUrl("type.googleapis.com/" + firestorePayload.getDescriptorForType().getFullName())
        .setValue(firestorePayload.toByteString())
        .build();

    CloudEvent event = CloudEventBuilder.v1()
        .withId("0")
        .withSubject("test subject")
        .withSource(URI.create("https://example.com"))
        .withType("google.cloud.firestore.document.v1.written")
        .withData(anyPayload.toByteArray())
        .build();

    new FirebaseFirestore().accept(event);

    assertThat(LOG_HANDLER.getStoredLogRecords().get(0).getMessage()).isEqualTo(
        "Function triggered by event on: " + event.getSource());
    assertThat(LOG_HANDLER.getStoredLogRecords().get(1).getMessage()).isEqualTo(
        "Event type: " + event.getType());
    assertThat(LOG_HANDLER.getStoredLogRecords().get(2).getMessage()).isEqualTo(
        "Old value:");
    assertThat(LOG_HANDLER.getStoredLogRecords().get(3).getMessage()).isEqualTo(
        oldValue.toString());
    assertThat(LOG_HANDLER.getStoredLogRecords().get(4).getMessage()).isEqualTo(
        "New value:");
    assertThat(LOG_HANDLER.getStoredLogRecords().get(5).getMessage()).isEqualTo(
        newValue.toString());
  }
}
