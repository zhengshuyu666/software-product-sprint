// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    
    // If the duration is longer than a day, no options
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration())
        return Arrays.asList();

    // Collect confilct events and sort
    List<TimeRange> confilctEvents = new ArrayList<TimeRange>();
    for (Event event : events) {
        for (String attendee : event.getAttendees()) {
            if (request.getAttendees().contains(attendee)) {
                confilctEvents.add(event.getWhen());
                break;
            }
        }
    }
    confilctEvents.sort(TimeRange.ORDER_BY_START);

    // Vacancies of the day
    List<TimeRange> vacancies = new ArrayList();
    vacancies.add(TimeRange.WHOLE_DAY);

    // Traverse the confilct event list and split the vacancy
    for (TimeRange eventspan : confilctEvents) {
        for (TimeRange vacancy : vacancies) {
            if (vacancy.equals(eventspan)) {
                // If the event duration equals the vacancy, remove and break
                
                vacancies.remove(vacancy);
                break;

            } else if (eventspan.contains(vacancy)) {
                // If the event duration contains the vacancy, remove the vacancy
                
                vacancies.remove(vacancy);

            } else if (vacancy.contains(eventspan)) {
                // If the vacancy contains the event duration, split the vacancy into two options (before and after the event)
                
                TimeRange before = TimeRange.fromStartEnd(vacancy.start(), eventspan.start(), false);
                TimeRange after = TimeRange.fromStartEnd(eventspan.end(), vacancy.end(), false);
                vacancies.remove(vacancy);
                if (before.duration() > 0 && before.duration() >= request.getDuration())
                    vacancies.add(before);
                if (after.duration() > 0 && after.duration() >= request.getDuration())
                    vacancies.add(after);
                break;

            } else if (vacancy.overlaps(eventspan)) {
                // If the vacancy and the event overlaps, remove the overlap
                
                if (vacancy.start() <= eventspan.start()) {
                    TimeRange before = TimeRange.fromStartEnd(vacancy.start(), eventspan.start(), false);
                    if (before.duration() > 0 && before.duration() >= request.getDuration())
                        vacancies.add(before);
                } else {
                    TimeRange after = TimeRange.fromStartEnd(eventspan.end(), vacancy.end(), false);
                    if (after.duration() > 0 && after.duration() >= request.getDuration())
                        vacancies.add(after);
                }
                vacancies.remove(vacancy);
                
            }
        }
    }

    return vacancies;
  }
}
