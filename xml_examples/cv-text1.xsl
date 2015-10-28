<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema">
	<xsl:output method="text" />

	<xsl:param name="new_line">
		<xsl:text>&#xD;&#xa;</xsl:text>
	</xsl:param>

	<xsl:param name="tab">
		<xsl:text>&#x9;</xsl:text>
	</xsl:param>

	<xsl:param name="space">
		<xsl:text>&#160;</xsl:text>
	</xsl:param>

	<xsl:template match="/">
		<xsl:text>Biography</xsl:text>
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$new_line" />
		<xsl:text>Personal info: </xsl:text>
		<xsl:apply-templates select="biography/personal_info" />
		<xsl:value-of select="$new_line" />
		<xsl:text>Contact info:</xsl:text>
		<xsl:apply-templates select="biography/contact_info" />
		<xsl:value-of select="$new_line" />
		<xsl:text>Education:</xsl:text>
		<xsl:apply-templates select="biography/education/school" />
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$new_line" />
		<xsl:text>Courses and certificates:</xsl:text>
		<xsl:apply-templates select="biography/courses/course" />
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$new_line" />
		<xsl:text>Experience:</xsl:text>
		<xsl:apply-templates select="biography/career/experience" />
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$new_line" />
		<xsl:text>Skills:</xsl:text>
		<xsl:apply-templates select="biography/skills/skill" />
		<xsl:value-of select="$new_line" />
	</xsl:template>

	<xsl:template match="personal_info">
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>Full name:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:apply-templates select="title_before" />
		<xsl:value-of select="first_name" />
		<xsl:value-of select="$space" />
		<xsl:value-of select="surname" />
		<xsl:apply-templates select="title_after" />
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>Birth date:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="birth_date" />
		<xsl:value-of select="$new_line" />
	</xsl:template>

	<xsl:template match="title_before">
		<xsl:if test=". != ''">
			<xsl:value-of select="." />
			<xsl:value-of select="$space" />
		</xsl:if>
	</xsl:template>

	<xsl:template match="title_after">
		<xsl:if test=". != ''">
			<xsl:value-of select="$space" />
			<xsl:value-of select="." />
		</xsl:if>
	</xsl:template>

	<xsl:template match="contact_info">
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>Email:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="email" />
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>Phone:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="phone" />
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>Address:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:apply-templates select="address" />
	</xsl:template>

	<xsl:template match="address">
		<xsl:value-of select="street" />
		<xsl:text>,</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="postal_code" />
		<xsl:value-of select="$space" />
		<xsl:value-of select="city" />
		<xsl:text>,</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="country" />
		<xsl:value-of select="$new_line" />
	</xsl:template>

	<xsl:template match="school">
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>Name:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="name" />
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>Faculty:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="faculty" />
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>Profession:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="profession" />
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>Degree:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:choose>
			<xsl:when test="degree = 'isced_2011_8'">
				<xsl:text>Doctoral or equivalent</xsl:text>
			</xsl:when>
			<xsl:when test="degree = 'isced_2011_7'">
				<xsl:text>Master or equivalent</xsl:text>
			</xsl:when>
			<xsl:when test="degree = 'isced_2011_6'">
				<xsl:text>Bachelor or equivalent</xsl:text>
			</xsl:when>
			<xsl:when test="degree = 'isced_2011_5'">
				<xsl:text>Short-cycle tertiary education</xsl:text>
			</xsl:when>
			<xsl:when test="degree = 'isced_2011_4'">
				<xsl:text>Post-secondary non-tertiary education</xsl:text>
			</xsl:when>
			<xsl:when test="degree = 'isced_2011_3'">
				<xsl:text>Upper secondary education</xsl:text>
			</xsl:when>
			<xsl:when test="degree = 'isced_2011_2'">
				<xsl:text>Lower secondary education</xsl:text>
			</xsl:when>
			<xsl:when test="degree = 'isced_2011_1'">
				<xsl:text>Primary education</xsl:text>
			</xsl:when>
		</xsl:choose>
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>Start date:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="start_date" />
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>End date:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="end_date" />
		<xsl:if test="position() != last()">
			<xsl:value-of select="$new_line" />
		</xsl:if>
	</xsl:template>

	<xsl:template match="course">
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>Type:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:choose>
			<xsl:when test="@type = 0">
				<xsl:text>Course</xsl:text>
			</xsl:when>
			<xsl:when test="@type = 1">
				<xsl:text>Certificate</xsl:text>
			</xsl:when>
		</xsl:choose>
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>Organization:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="organization" />
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>Name:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="name" />
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>Valid from:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="valid_from" />
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>Valid to:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="valid_to" />
		<xsl:if test="position() != last()">
			<xsl:value-of select="$new_line" />
		</xsl:if>
	</xsl:template>

	<xsl:template match="experience">
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>Employer:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="employer" />
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>Profession:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="profession" />
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>Description:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="description" />
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>Start date:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="start_date" />
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>Projects:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:apply-templates select="projects/project" />
		<xsl:if test="position() != last()">
			<xsl:value-of select="$new_line" />
		</xsl:if>
	</xsl:template>

	<xsl:template match="project">
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:value-of select="$tab" />
		<xsl:text>Name:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="name" />
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:value-of select="$tab" />
		<xsl:text>Description:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="description" />
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:value-of select="$tab" />
		<xsl:text>Role:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="role" />
		<xsl:if test="position() != last()">
			<xsl:value-of select="$new_line" />
		</xsl:if>
	</xsl:template>

	<xsl:template match="skill">
		<xsl:value-of select="$new_line" />
		<xsl:value-of select="$tab" />
		<xsl:text>Name:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="name" />
		<xsl:text>,</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:text>Level:</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="level" />
		<xsl:text>,</xsl:text>
		<xsl:value-of select="$space" />
		<xsl:value-of select="years" />
		<xsl:value-of select="$space" />
		<xsl:text>years</xsl:text>
	</xsl:template>

</xsl:stylesheet>