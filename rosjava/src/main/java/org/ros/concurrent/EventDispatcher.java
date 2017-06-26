/*
 * Copyright (C) 2012 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.concurrent;


/**
 * @author damonkohler@google.com (Damon Kohler)
 * 
 * @param <T>
 *          the listener type
 */
public class EventDispatcher<T> extends CancellableLoop {

  private final T listener;
  private final CircularBlockingDeque<SignalRunnable<T>> events;
  private volatile boolean canceled = false;
  
  public EventDispatcher(T listener, int queueCapacity) {
    this.listener = listener;
    events = new CircularBlockingDeque<SignalRunnable<T>>(queueCapacity);
  }

  public void signal(final SignalRunnable<T> signalRunnable) {
	  if(!canceled)  // stop adding signals when canceled
		  events.addLast(signalRunnable);
  }

  @Override
  public void loop() throws InterruptedException {
    SignalRunnable<T> signalRunnable = events.takeFirst();
    signalRunnable.run(listener);
  }
  
  @Override
  public void cancel() {
	  canceled = true;
	  long t = System.currentTimeMillis();
	  while(!events.isEmpty() && System.currentTimeMillis()-t < 5000){ // wait until all events are processed (timeout of 5s?)
		  // wait
	  }
	  super.cancel(); //  and then interupt
  }
}