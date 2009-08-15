/**********************************************************************************
*
* $Id:$
*
***********************************************************************************
*
* Copyright (c) 2008, 2009 The Regents of the University of California
*
* Licensed under the
* Educational Community License, Version 2.0 (the "License"); you may
* not use this file except in compliance with the License. You may
* obtain a copy of the License at
* 
* http://www.osedu.org/licenses/ECL-2.0
* 
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an "AS IS"
* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
* or implied. See the License for the specific language governing
* permissions and limitations under the License.
*
**********************************************************************************/
package org.sakaiproject.gradebook.gwt.sakai;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.sakaiproject.gradebook.gwt.sakai.model.GradeStatistics;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Gradebook;

public interface GradeCalculations {

	public Double calculateEqualWeight(int numberOfItems);
	
	public Double calculateItemWeightAsPercentage(Double requestedItemWeight, Double requestedItemPoints);
	
	public BigDecimal calculateItemGradePercent(BigDecimal percentGrade, BigDecimal sumCategoryPercents, BigDecimal assignmentWeight);
	
	public String convertPercentageToLetterGrade(BigDecimal percentage);
	
	public Double convertLetterGradeToPercentage(String letterGrade);
	
	public BigDecimal getNewPointsGrade(Double pointValue, Double maxPointValue, Double maxPointStartValue);
	
	public BigDecimal getPercentAsPointsEarned(Assignment assignment, Double percentage);
	
	/**
	 * Result = PointsEarned * 100 / PointsPossible
	 * 
	 * @param assignment an assignment
	 * @param assignmentGradeRecord an assignmentGradeRecord that is linked to the assignment, for a specific user 
	 * 
	 * @return points earned as percent for an assignment, or null if assignment or assignmentGradeRecord are null
	 */
	public BigDecimal getPointsEarnedAsPercent(Assignment assignment, AssignmentGradeRecord assignmentGradeRecord);
	

	/**
	 * Result = The Category Weight while taking into consideration the category "constraints" such as [isRemove, ...]
	 * 
	 * @param category a category
	 * 
	 * @return the category weight, or null if the category is null
	 */
	public BigDecimal getCategoryWeight(Category category);
	
	
	public BigDecimal getCourseGrade(Gradebook gradebook, Collection<?> items, Map<Long, AssignmentGradeRecord> assignmentGradeRecordMap, boolean isExtraCreditScaled);
	
	public GradeStatistics calculateStatistics(List<BigDecimal> gradeList, BigDecimal sum);
	
}
