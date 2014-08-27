/*
 * Autopsy Forensic Browser
 *
 * Copyright 2013-14 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.imageanalyzer.grouping;

import java.util.Arrays;
import java.util.Comparator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javax.swing.SortOrder;
import static javax.swing.SortOrder.ASCENDING;
import static javax.swing.SortOrder.DESCENDING;
import org.apache.commons.lang3.StringUtils;
import org.sleuthkit.autopsy.imageanalyzer.ImageAnalyzerController;
import org.sleuthkit.autopsy.imageanalyzer.datamodel.DrawableAttribute;
import static org.sleuthkit.autopsy.imageanalyzer.datamodel.DrawableAttribute.AttributeName.TAGS;
import org.sleuthkit.datamodel.TagName;

/** enum of possible properties to sort groups by. This is the model for the
 * drop down in {@link  EurekaToolbar} as well as each enum value having the
 * stategy ({@link  Comparator}) for sorting the groups */
public enum GroupSortBy implements ComparatorProvider {

    /** sort the groups by the number of files in each
     * sort the groups by the number of files in each */
    FILE_COUNT("Group Size", true, "folder-open-image.png") {
                @Override
        public Comparator<Grouping> getGrpComparator(DrawableAttribute attr, final SortOrder sortOrder) {
                    Comparator<Grouping> comparingInt = Comparator.comparingInt(Grouping::getSize);
            return sortOrder == ASCENDING ? comparingInt : comparingInt.reversed();
        }

                @Override
                public <A extends Comparable> Comparator<A> getValueComparator(final DrawableAttribute<A> attr, final SortOrder sortOrder) {
                    return (A v1, A v2) -> {
                        Grouping g1 = ImageAnalyzerController.getDefault().getGroupManager().getGroupForKey(new GroupKey(attr, v1));
                        Grouping g2 = ImageAnalyzerController.getDefault().getGroupManager().getGroupForKey(new GroupKey(attr, v2));
                        return getGrpComparator(attr, sortOrder).compare(g1, g2);
                    };
                }
            },
    /** sort
     * the groups by the natural order of the grouping value ( eg group them by
     * path alphabetically ) */
    GROUP_BY_VALUE("Group Name", true, "folder-rename.png") {
                @Override
        public Comparator<Grouping> getGrpComparator(final DrawableAttribute attr, final SortOrder sortOrder) {
            return (Grouping o1, Grouping o2) -> {

                        final Comparable c1 = o1.groupKey.getValue();
                        final Comparable c2 = o2.groupKey.getValue();
                        final boolean isTags = attr.attrName == TAGS;

                switch (sortOrder) {
                    case ASCENDING:
                        return c1.compareTo(c2);
                    case DESCENDING:
                        return c1.compareTo(c2) * -1;
                    default: //unsorted
                        return 0;
                }
            };
        }

        @Override
        public <A extends Comparable> Comparator<A> getValueComparator(final DrawableAttribute<A> attr, final SortOrder sortOrder) {
            return (A o1, A o2) -> {
                Comparable c1;
                Comparable c2;
                switch (attr.attrName) {
                    case TAGS:
                        c1 = ((TagName) o1).getDisplayName();
                        c2 = ((TagName) o2).getDisplayName();
                        break;
                    default:
                        c1 = (Comparable) o1;
                        c2 = (Comparable) o2;
                }

                        switch (sortOrder) {
                            case ASCENDING:
                                return c1.compareTo(c2);
                            case DESCENDING:
                                return c1.compareTo(c2) * -1;
                            default: //unsorted
                                return 0;
                        }
                    };
                }
            },
    /** don't sort the groups just use what ever
     * order they come in (ingest
     * order) */
    /** don't sort the groups just use what ever order they come
     * in (ingest
     * order) */
    NONE("None", false, "prohibition.png") {
                @Override
        public Comparator<Grouping> getGrpComparator(DrawableAttribute attr, SortOrder sortOrder) {
            return new NoOpComparator<>();
        }

                @Override
                public <A extends Comparable> Comparator<A> getValueComparator(DrawableAttribute<A> attr, final SortOrder sortOrder) {
                    return new NoOpComparator<>();
                }
            },
    /** sort
     * the groups by some priority metric to be determined and implemented */
    PRIORITY("Priority", false, "hashset_hits.png") {
                @Override
        public Comparator<Grouping> getGrpComparator(DrawableAttribute attr, SortOrder sortOrder) {
            return Comparator.nullsLast(Comparator.comparingDouble(Grouping::getHashHitDensity).thenComparing(Grouping::getSize).reversed());
        }

        @Override
        public <A extends Comparable> Comparator<A> getValueComparator(DrawableAttribute<A> attr, SortOrder sortOrder) {
            return (A v1, A v2) -> {
                Grouping g1 = ImageAnalyzerController.getDefault().getGroupManager().getGroupForKey(new GroupKey(attr, v1));
                Grouping g2 = ImageAnalyzerController.getDefault().getGroupManager().getGroupForKey(new GroupKey(attr, v2));

                        return getGrpComparator(attr, sortOrder).compare(g1, g2);
                    };
                }
            };

    /**
     * get a list of the values of this enum
     *
     * @return
     */
    public static ObservableList<GroupSortBy> getValues() {
        return FXCollections.observableArrayList(Arrays.asList(values()));

    }

    final private String displayName;

    private Image icon;

    private final String imageName;

    private final Boolean sortOrderEnabled;

    private GroupSortBy(String displayName, Boolean sortOrderEnabled, String imagePath) {
        this.displayName = displayName;
        this.sortOrderEnabled = sortOrderEnabled;
        this.imageName = imagePath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Image getIcon() {
        if (icon == null) {
            if (StringUtils.isBlank(imageName) == false) {
                this.icon = new Image("org/sleuthkit/autopsy/imageanalyzer/images/" + imageName, true);
            }
        }
        return icon;
    }

    public Boolean isSortOrderEnabled() {
        return sortOrderEnabled;
    }

    private static class NoOpComparator<A> implements Comparator<A> {

        @Override
        public int compare(A o1, A o2) {
            return 0;
        }
    }
}

/** * implementers of this interface must provide a method to compare
 * ({@link  Comparable}) values and Groupings based on an
 * {@link DrawableAttribute} and a {@link SortOrder}
 */
interface ComparatorProvider {

    <A extends Comparable> Comparator<A> getValueComparator(DrawableAttribute<A> attr, SortOrder sortOrder);

    <A> Comparator<Grouping> getGrpComparator(DrawableAttribute<A> attr, SortOrder sortOrder);
}
