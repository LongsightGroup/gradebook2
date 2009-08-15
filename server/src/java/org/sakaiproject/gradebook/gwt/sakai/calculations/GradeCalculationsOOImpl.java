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

package org.sakaiproject.gradebook.gwt.sakai.calculations;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.gradebook.gwt.sakai.GradeCalculations;
import org.sakaiproject.gradebook.gwt.sakai.model.GradeStatistics;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Gradebook;

public class GradeCalculationsOOImpl implements GradeCalculations {

	final static BigDecimal BIG_DECIMAL_100 = new BigDecimal("100.00000");
	public static final MathContext MATH_CONTEXT = new MathContext(10, RoundingMode.HALF_EVEN);
	public static Map<String, Double> letterGradePercentMap;
	
	public void init() {
		letterGradePercentMap = new HashMap<String, Double>();
		letterGradePercentMap.put("A+", Double.valueOf(98.3333333333d));
		letterGradePercentMap.put("A", Double.valueOf(95d));
		letterGradePercentMap.put("A-", Double.valueOf(91.6666666666d));
		letterGradePercentMap.put("B+", Double.valueOf(88.3333333333d));
		letterGradePercentMap.put("B", Double.valueOf(85d));
		letterGradePercentMap.put("B-", Double.valueOf(81.6666666666d));
		letterGradePercentMap.put("C+", Double.valueOf(78.3333333333d));
		letterGradePercentMap.put("C", Double.valueOf(75d));
		letterGradePercentMap.put("C-", Double.valueOf(71.6666666666d));
		letterGradePercentMap.put("D+", Double.valueOf(68.3333333333d));
		letterGradePercentMap.put("D", Double.valueOf(65d));
		letterGradePercentMap.put("D-", Double.valueOf(61.6666666666d));
		letterGradePercentMap.put("F", Double.valueOf(58.3333333333d));
	}

	public Double calculateEqualWeight(int numberOfItems) {
		if (numberOfItems <= 1)
			return Double.valueOf(1d);

		BigDecimal result = BigDecimal.ONE.divide(BigDecimal.valueOf(numberOfItems), MATH_CONTEXT);

		return Double.valueOf(result.doubleValue());
	}

	public Double calculateItemWeightAsPercentage(Double requestedItemWeight, Double requestedItemPoints) {
		BigDecimal weight = null;

		// Obviously, if the user asks for a non-null value, give it to them
		if (requestedItemWeight != null)
			weight = BigDecimal.valueOf(requestedItemWeight.doubleValue());
		else
			weight = BigDecimal.valueOf(requestedItemPoints.doubleValue());

		BigDecimal result = weight.divide(BIG_DECIMAL_100, MATH_CONTEXT);

		return Double.valueOf(result.doubleValue());
	}

	/*
	 *  For example: 
	 *  
	 *  (a) If percentGrade is 60, sumCategoryPercents is 100, and assignmentWeight is 0.25, then this item should be worth 15 % of the course grade
	 *  
	 *  	15 = ( 60 * .25 ) / 1
	 *  	
	 *  (b) If percentGrade is 60, sumCategoryPercents is 80, and assignmentWeight is 0.25, then this item should be worth > 15 % of the course grade
	 * 
	 * 		x  = ( 60 * .25 ) / .8
	 * 
	 */
	public BigDecimal calculateItemGradePercent(BigDecimal percentGrade, BigDecimal sumCategoryPercents, BigDecimal assignmentWeight) {

		if (percentGrade.compareTo(BigDecimal.ZERO) == 0 
				|| sumCategoryPercents.compareTo(BigDecimal.ZERO) == 0 
				|| assignmentWeight.compareTo(BigDecimal.ZERO) == 0)
			return BigDecimal.ZERO;

		BigDecimal categoryPercentRatio = sumCategoryPercents.divide(BigDecimal.valueOf(100d), MATH_CONTEXT);

		return assignmentWeight.multiply(percentGrade).divide(categoryPercentRatio, MATH_CONTEXT);
	}


	public GradeStatistics calculateStatistics(List<BigDecimal> gradeList, BigDecimal sum) {
		GradeStatistics statistics = new GradeStatistics();

		if (gradeList == null || gradeList.isEmpty())
			return statistics;

		BigDecimal count = BigDecimal.valueOf(gradeList.size());
		BigDecimal mean = null;

		if (count.compareTo(BigDecimal.ZERO) != 0)
			mean = sum.divide(count, RoundingMode.HALF_EVEN);

		BigDecimal mode = null;
		Integer highestFrequency = Integer.valueOf(0);
		Map<BigDecimal, Integer> frequencyMap = new HashMap<BigDecimal, Integer>();
		BigDecimal standardDeviation = null;
		// Once we have the mean course grade, we can calculate the standard deviation from that mean
		// That is, for the equation S = sqrt(A/c)
		if (gradeList != null && mean != null) {
			BigDecimal sumOfSquareOfDifferences = BigDecimal.ZERO;
			for (BigDecimal courseGrade : gradeList) {
				// Take the square of the difference and add it to the sum, A 
				BigDecimal difference = courseGrade.subtract(mean);
				BigDecimal square = difference.multiply(difference);
				sumOfSquareOfDifferences = sumOfSquareOfDifferences.add(square);

				Integer frequency = frequencyMap.get(courseGrade);
				if (frequency == null)
					frequency = Integer.valueOf(1);
				else
					frequency = Integer.valueOf(1 + frequency.intValue());

				frequencyMap.put(courseGrade, frequency);

				if (frequency.compareTo(highestFrequency) > 0) {
					highestFrequency = frequency;
					mode = courseGrade;
				}
			}

			if (count.compareTo(BigDecimal.ZERO) != 0 && sumOfSquareOfDifferences.compareTo(BigDecimal.ZERO) != 0) {
				BigDecimal fraction = sumOfSquareOfDifferences.divide(count, RoundingMode.HALF_EVEN);
				BigSquareRoot squareRoot = new BigSquareRoot();
				standardDeviation = squareRoot.get(fraction);
			}
		}

		BigDecimal median = null;
		if (gradeList != null && gradeList.size() > 0) {
			if (gradeList.size() == 1) {
				median = gradeList.get(0);
			} else {
				Collections.sort(gradeList);
				int middle = gradeList.size() / 2;
				if (gradeList.size() % 2 == 0) {
					// If we have an even number of elements then grab the middle two
					BigDecimal first = gradeList.get(middle - 1);
					BigDecimal second = gradeList.get(middle);

					BigDecimal s = first.add(second);

					if (s.compareTo(BigDecimal.ZERO) == 0)
						median = BigDecimal.ZERO;
					else
						median = s.divide(new BigDecimal("2"), RoundingMode.HALF_EVEN);
				} else {
					// If we have an odd number of elements, simply choose the middle one
					median = gradeList.get(middle);
				}
			}
		}

		statistics.setMean(mean);
		statistics.setMedian(median);
		statistics.setMode(mode);
		statistics.setStandardDeviation(standardDeviation);

		return statistics;
	}
	
	public String convertPercentageToLetterGrade(BigDecimal percentage) {
		String letterGrade = null;
		
		if (percentage != null) {
			
			BigDecimal minus60 = percentage.subtract(BigDecimal.valueOf(60d));
			
			if (minus60.compareTo(BigDecimal.ZERO) < 0)
				return "F";
			
			BigDecimal decimal = minus60.divide(BigDecimal.valueOf(10d), MATH_CONTEXT);
			
			if (decimal.compareTo(BigDecimal.valueOf(3.7d)) >= 0)
				return "A+";
			if (decimal.compareTo(BigDecimal.valueOf(3.3d)) >= 0)
				return "A";
			if (decimal.compareTo(BigDecimal.valueOf(3d)) >= 0)
				return "A-";
			if (decimal.compareTo(BigDecimal.valueOf(2.7d)) >= 0)
				return "B+";
			if (decimal.compareTo(BigDecimal.valueOf(2.3d)) >= 0)
				return "B";
			if (decimal.compareTo(BigDecimal.valueOf(2d)) >= 0)
				return "B-"; 
			if (decimal.compareTo(BigDecimal.valueOf(1.7d)) >= 0)
				return "C+";
			if (decimal.compareTo(BigDecimal.valueOf(1.3d)) >= 0)
				return "C";
			if (decimal.compareTo(BigDecimal.valueOf(1d)) >= 0)
				return "C-"; 
			if (decimal.compareTo(BigDecimal.valueOf(0.7d)) >= 0)
				return "D+";
			if (decimal.compareTo(BigDecimal.valueOf(0.3d)) >= 0)
				return "D";
			
			return "D-"; 
		}
		
		return letterGrade;
	}
	
	public Double convertLetterGradeToPercentage(String letterGrade) {
		Double percentage = null;
		
		if (letterGrade != null) {
			return letterGradePercentMap.get(letterGrade);
		}
		
		return percentage;
	}

	public BigDecimal getCategoryWeight(Category category) {
		BigDecimal categoryWeight = null;

		if (null == category || isDeleted(category)) {
			return null;
		}

		Gradebook gradebook = category.getGradebook();

		switch (gradebook.getCategory_type()) {
			case GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY:
				if (null == category.getWeight() || isUnweighted(category))
					return null;
				categoryWeight = new BigDecimal(category.getWeight().toString());
				break;
			default:
				categoryWeight = BigDecimal.ZERO;

				List<Assignment> assignments = (List<Assignment>)category.getAssignmentList();
				if (assignments != null) {
					for (Assignment assignment : assignments) {
						BigDecimal assignmentWeight = getAssignmentWeight(assignment);

						if (assignmentWeight != null)
							categoryWeight = categoryWeight.add(assignmentWeight);
					}
				}
				break;
		}

		return categoryWeight;
	}

	@SuppressWarnings("unchecked")
	public BigDecimal getCourseGrade(Gradebook gradebook, Collection<?> items, Map<Long, AssignmentGradeRecord> assignmentGradeRecordMap, boolean isExtraCreditScaled) {
		boolean isWeighted = true;
		switch (gradebook.getCategory_type()) {
			case GradebookService.CATEGORY_TYPE_NO_CATEGORY:
				return getNoCategoriesCourseGrade((Collection<Assignment>)items, assignmentGradeRecordMap, isExtraCreditScaled);
			case GradebookService.CATEGORY_TYPE_ONLY_CATEGORY:
				isWeighted = false;
			case GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY:
				return getCategoriesCourseGrade((Collection<Category>)items, assignmentGradeRecordMap, isWeighted, isExtraCreditScaled);
		}

		return null;
	}

	private BigDecimal getNoCategoriesCourseGrade(Collection<Assignment> assignments, Map<Long, AssignmentGradeRecord> assignmentGradeRecordMap, boolean isExtraCreditScaled) {
		List<GradeRecordCalculationUnit> gradeRecordUnits = new ArrayList<GradeRecordCalculationUnit>();

		populateGradeRecordUnits(assignments, gradeRecordUnits, assignmentGradeRecordMap);

		GradebookCalculationUnit gradebookUnit = new GradebookCalculationUnit();

		return gradebookUnit.calculatePointsBasedCourseGrade(gradeRecordUnits, isExtraCreditScaled);
	}

	@SuppressWarnings("unchecked")
	private BigDecimal getCategoriesCourseGrade(Collection<Category> categoriesWithAssignments, Map<Long, AssignmentGradeRecord> assignmentGradeRecordMap,
			boolean isWeighted, boolean isExtraCreditScaled) {

		if (categoriesWithAssignments == null && assignmentGradeRecordMap != null) 
			categoriesWithAssignments = generateCategoriesWithAssignments(assignmentGradeRecordMap);

		if (categoriesWithAssignments == null || assignmentGradeRecordMap == null)
			return null;

		Map<String, CategoryCalculationUnit> categoryUnitMap = new HashMap<String, CategoryCalculationUnit>();

		Map<String, List<GradeRecordCalculationUnit>> categoryGradeUnitListMap = new HashMap<String, List<GradeRecordCalculationUnit>>();

		if (categoriesWithAssignments != null) {
			for (Category category : categoriesWithAssignments) {

				if (category == null || category.isRemoved() || isUnweighted(category))
					continue;

				String categoryKey = String.valueOf(category.getId());

				BigDecimal categoryWeight = getCategoryWeight(category);
				CategoryCalculationUnit categoryCalculationUnit = new CategoryCalculationUnit(categoryWeight, Integer.valueOf(category.getDrop_lowest()), category.isExtraCredit());
				categoryUnitMap.put(categoryKey, categoryCalculationUnit);

				List<GradeRecordCalculationUnit> gradeRecordUnits = new ArrayList<GradeRecordCalculationUnit>();

				List<Assignment> assignments = category.getAssignmentList();
				if (assignments == null)
					continue;

				populateGradeRecordUnits(assignments, gradeRecordUnits, assignmentGradeRecordMap);

				categoryGradeUnitListMap.put(categoryKey, gradeRecordUnits);

			} // for
		}

		GradebookCalculationUnit gradebookUnit = new GradebookCalculationUnit(categoryUnitMap);

		if (isWeighted)
			return gradebookUnit.calculateWeightedCourseGrade(categoryGradeUnitListMap);

		return gradebookUnit.calculatePointsBasedCourseGrade(categoryGradeUnitListMap, isExtraCreditScaled);
	}


	private void populateGradeRecordUnits(Collection<Assignment> assignments, List<GradeRecordCalculationUnit> gradeRecordUnits, 
			Map<Long, AssignmentGradeRecord> assignmentGradeRecordMap) {

		if (assignmentGradeRecordMap == null) 
			return;

		for (Assignment assignment : assignments) {

			if (assignment.isRemoved())
				continue;

			if (isUnweighted(assignment))
				continue;


			AssignmentGradeRecord assignmentGradeRecord = assignmentGradeRecordMap.get(assignment.getId());

			if (isGraded(assignmentGradeRecord)) {
				// Make sure it's not excused
				if (!isExcused(assignmentGradeRecord)) {

					BigDecimal pointsEarned = new BigDecimal(assignmentGradeRecord.getPointsEarned().toString());
					BigDecimal pointsPossible = new BigDecimal(assignment.getPointsPossible().toString());
					BigDecimal assignmentWeight = getAssignmentWeight(assignment);

					GradeRecordCalculationUnit gradeRecordUnit = new GradeRecordCalculationUnit(pointsEarned, 
							pointsPossible, assignmentWeight, assignment.isExtraCredit()) {

						@Override
						public void setDropped(boolean isDropped) {
							super.setDropped(isDropped);

							AssignmentGradeRecord gradeRecord = (AssignmentGradeRecord)getActualRecord();

							gradeRecord.setDropped(Boolean.valueOf(isDropped));
						}

					};

					gradeRecordUnit.setActualRecord(assignmentGradeRecord);

					gradeRecordUnits.add(gradeRecordUnit);
				}
			}
		}
	}


	public BigDecimal getNewPointsGrade(Double pointValue, Double maxPointValue, Double maxPointStartValue) {

		BigDecimal max = new BigDecimal(maxPointValue.toString());
		BigDecimal maxStart = new BigDecimal(maxPointStartValue.toString());
		BigDecimal ratio = BigDecimal.ZERO; 
		if (maxStart.compareTo(BigDecimal.ZERO) != 0)
		{
			ratio = max.divide(maxStart, MATH_CONTEXT);
		}
		BigDecimal points = new BigDecimal(pointValue.toString());

		return points.multiply(ratio, MATH_CONTEXT);
	}

	public BigDecimal getPercentAsPointsEarned(Assignment assignment, Double percentage) {
		BigDecimal pointsEarned = null;

		if (percentage != null) {
			BigDecimal percent = new BigDecimal(percentage.toString());
			BigDecimal maxPoints = new BigDecimal(assignment.getPointsPossible().toString());
			pointsEarned = percent.divide(BIG_DECIMAL_100, MATH_CONTEXT).multiply(maxPoints);
		}

		return pointsEarned;	
	}

	public BigDecimal getPointsEarnedAsPercent(Assignment assignment, AssignmentGradeRecord assignmentGradeRecord) {
		BigDecimal percentageEarned = null;
		BigDecimal pointsEarned = null;
		BigDecimal pointsPossible = null;

		if (isBlank(assignment, assignmentGradeRecord)) {
			return percentageEarned;
		}

		pointsEarned = new BigDecimal(assignmentGradeRecord.getPointsEarned().toString());
		if (assignment.getPointsPossible() != null) {
			pointsPossible = new BigDecimal(assignment.getPointsPossible().toString());
			percentageEarned = pointsEarned.multiply(BIG_DECIMAL_100).divide(pointsPossible, MATH_CONTEXT);
		}
		return percentageEarned;
	}

	private Collection<Category> generateCategoriesWithAssignments(Map<Long, AssignmentGradeRecord>  assignmentGradeRecordMap) {
		Collection<Category> categoriesWithAssignments = new ArrayList<Category>();

		Map<Long, Category> categoryMap = new HashMap<Long, Category>();
		for (AssignmentGradeRecord gradeRecord : assignmentGradeRecordMap.values()) {
			if (gradeRecord != null) {
				Assignment assignment = gradeRecord.getAssignment();
				if (assignment != null) {
					Category category = assignment.getCategory();
					if (category != null) {
						Long categoryId = category.getId();

						Category storedCategory = categoryMap.get(categoryId);

						if (storedCategory != null)
							category = storedCategory;
						else {
							categoryMap.put(categoryId, category);
							category.setAssignmentList(new ArrayList<Assignment>());
						}

						category.getAssignmentList().add(assignment);
					}
				}
			}

			categoriesWithAssignments = categoryMap.values();
		}

		return categoriesWithAssignments;
	}

	private BigDecimal getAssignmentWeight(Assignment assignment) {

		BigDecimal assignmentWeight = null;

		// If the assignment doesn't exist or has no weight then we return null
		if (null == assignment || isDeleted(assignment)) 
			return null;

		Gradebook gradebook = assignment.getGradebook();

		switch (gradebook.getCategory_type()) {
			case GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY:
				if (null == assignment.getAssignmentWeighting() || isUnweighted(assignment)) 
					return null;
				assignmentWeight = new BigDecimal(assignment.getAssignmentWeighting().toString());
				break;
			default:
				if (null == assignment.getPointsPossible())
					return null;

				assignmentWeight = new BigDecimal(assignment.getPointsPossible().toString());
				break;
		}

		return assignmentWeight;
	}

	private boolean isBlank(Assignment assignment, AssignmentGradeRecord assignmentGradeRecord) {
		return null == assignment || null == assignmentGradeRecord || null == assignmentGradeRecord.getPointsEarned();
	}

	private boolean isDeleted(Assignment assignment) {
		return assignment.isRemoved();
	}

	private boolean isDeleted(Category category) {
		return category.isRemoved();
	}

	private boolean isExtraCredit(Assignment assignment) {
		return assignment.isExtraCredit() == null ? false : assignment.isExtraCredit().booleanValue();
	}

	private boolean isGraded(AssignmentGradeRecord assignmentGradeRecord) {
		return assignmentGradeRecord != null && assignmentGradeRecord.getPointsEarned() != null;
	}

	private boolean isDropped(AssignmentGradeRecord assignmentGradeRecord) {
		return assignmentGradeRecord != null && assignmentGradeRecord.isDropped() != null && assignmentGradeRecord.isDropped().booleanValue();
	}

	private boolean isExcused(AssignmentGradeRecord assignmentGradeRecord) {
		return assignmentGradeRecord.isExcluded() == null ? false : assignmentGradeRecord.isExcluded().booleanValue();
	}


	private boolean isNormalCredit(Assignment assignment) {
		boolean isExtraCredit = isExtraCredit(assignment);
		return assignment.isCounted() && !assignment.isRemoved() && !isExtraCredit;
	}

	private boolean isUnweighted(Assignment assignment) {
		return assignment.isUnweighted() == null ? false : assignment.isUnweighted().booleanValue();
	}

	private boolean isUnweighted(Category category) {
		return category.isUnweighted() == null ? false : category.isUnweighted().booleanValue();
	}

}
