package org.sakaiproject.gradebook.gwt.test.mock;

import org.sakaiproject.gradebook.gwt.client.exceptions.InvalidInputException;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel.CategoryType;

public class ToolFacadeNoCategoriesPercentageTest extends AbstractToolFacadePercentageTest {

	@Override
	protected void initialize() throws InvalidInputException {
		initialize(CategoryType.NO_CATEGORIES);
	}
	
	/*
	 * Overall grade will be points based 20 + 15 + 5 + 10 + 10 + 8 + 2 = 70 points out of 100 possible
	 */
	// FIXME: We probably shouldn't use "points" in this case, since we're in percentages mode, but that's the current setup
	public void testRepresentativeGrade() throws InvalidInputException {
		setRepresentativePercentagesGrade();
		checkCourseGrade("C- (70.00%) ");
	}
}
