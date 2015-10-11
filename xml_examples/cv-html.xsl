<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema">
	<xsl:output method="html" />
	<xsl:template match="/">
		<html>
			<body>
				<h2>Biography</h2>
				<table border="1">
					<tr>
						<td>Personal info</td>
						<td>
							<xsl:apply-templates select="biography/personal_info" />
						</td>
					</tr>
					<tr>
						<td>Contact info:</td>
						<td>
							<xsl:apply-templates select="biography/contact_info" />
						</td>
					</tr>
					<tr>
						<td>Education</td>
						<td>
							<xsl:apply-templates select="biography/education/school" />
						</td>
					</tr>
					<tr>
						<td>Courses and certificates</td>
						<td>
							<xsl:apply-templates select="biography/courses/course" />
						</td>
					</tr>
					<tr>
						<td>Experience</td>
						<td>
							<xsl:apply-templates select="biography/career/experience" />
						</td>
					</tr>
					<tr>
						<td>Skills</td>
						<td>
							<xsl:apply-templates select="biography/skills/skill" />
						</td>
					</tr>

				</table>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="personal_info">
		<p>
			Full name:
			<xsl:apply-templates select="title_before" />
			<xsl:value-of select="first_name" />
			&#160;
			<xsl:value-of select="surname" />
			<xsl:apply-templates select="title_after" />
		</p>
		<p>
			Birth date:
			<xsl:value-of select="format-date(birth_date,'[D01].[M01].[Y0001]')" />
		</p>
	</xsl:template>

	<xsl:template match="title_before">
		<xsl:if test=". != ''">
			<xsl:value-of select="." />
			&#160;
		</xsl:if>
	</xsl:template>

	<xsl:template match="title_after">
		<xsl:if test=". != ''">
			&#160;
			<xsl:value-of select="." />
		</xsl:if>
	</xsl:template>

	<xsl:template match="contact_info">
		<p>
			Email:
			<xsl:value-of select="email" />
		</p>
		<p>
			Phone:
			<xsl:value-of select="phone" />
		</p>
		<p>
			Address:
			<xsl:apply-templates select="address" />
		</p>
	</xsl:template>

	<xsl:template match="address">
		<xsl:value-of select="street" />
		<xsl:text>, </xsl:text>
		<xsl:value-of select="postal_code" />
		&#160;
		<xsl:value-of select="city" />
		<xsl:text>, </xsl:text>
		<xsl:value-of select="country" />
	</xsl:template>

	<xsl:template match="school">
		<p>
			Name:
			<xsl:value-of select="name" />
		</p>
		<p>
			Faculty:
			<xsl:value-of select="faculty" />
		</p>
		<p>
			Profession:
			<xsl:value-of select="profession" />
		</p>
		<p>
			Degree:
			<xsl:choose>
				<xsl:when test="degree = 'isced_2011_8'">
					Doctoral or equivalent
				</xsl:when>
				<xsl:when test="degree = 'isced_2011_7'">
					Master or equivalent
				</xsl:when>
				<xsl:when test="degree = 'isced_2011_6'">
					Bachelor or equivalent
				</xsl:when>
				<xsl:when test="degree = 'isced_2011_5'">
					Short-cycle tertiary education
				</xsl:when>
				<xsl:when test="degree = 'isced_2011_4'">
					Post-secondary non-tertiary education
				</xsl:when>
				<xsl:when test="degree = 'isced_2011_3'">
					Upper secondary education
				</xsl:when>
				<xsl:when test="degree = 'isced_2011_2'">
					Lower secondary education
				</xsl:when>
				<xsl:when test="degree = 'isced_2011_1'">
					Primary education
				</xsl:when>
			</xsl:choose>
		</p>
		<p>
			Start date:
			<xsl:value-of select="format-date(start_date,'[D01].[M01].[Y0001]')" />
		</p>
		<p>
			End date:
			<xsl:value-of select="format-date(end_date,'[D01].[M01].[Y0001]')" />
		</p>
		<xsl:if test="position() != last()">
			<br />
		</xsl:if>
	</xsl:template>

	<xsl:template match="course">
		<p>
			Type:
			<xsl:choose>
				<xsl:when test="@type = 0">
					Course
				</xsl:when>
				<xsl:when test="@type = 1">
					Certificate
				</xsl:when>
			</xsl:choose>
		</p>
		<p>
			Organization:
			<xsl:value-of select="organization" />
		</p>
		<p>
			Name:
			<xsl:value-of select="name" />
		</p>
		<p>
			Valid from:
			<xsl:value-of select="format-date(valid_from,'[D01].[M01].[Y0001]')" />
		</p>
		<p>
			Valid to:
			<xsl:value-of select="format-date(valid_to,'[D01].[M01].[Y0001]')" />
		</p>
		<xsl:if test="position() != last()">
			<br />
		</xsl:if>
	</xsl:template>

	<xsl:template match="experience">
		<p>
			Employer:
			<xsl:value-of select="employer" />
		</p>
		<p>
			Profession:
			<xsl:value-of select="profession" />
		</p>
		<p>
			Description:
			<xsl:value-of select="description" />
		</p>
		<p>
			Start date:
			<xsl:value-of select="format-date(start_date,'[D01].[M01].[Y0001]')" />
		</p>
		<table>
			<tr>
				<td valign="top">Projects:</td>
				<td>
					<xsl:apply-templates select="projects/project" />
				</td>
			</tr>
		</table>
		<xsl:text>
</xsl:text>
	</xsl:template>

	<xsl:template match="project">
		<p>
			Name:
			<xsl:value-of select="name" />
		</p>
		<p>
			Description:
			<xsl:value-of select="description" />
		</p>
		<p>
			Role:
			<xsl:value-of select="role" />
		</p>
	</xsl:template>

	<xsl:template match="skill">
		<p>
			Name:
			<xsl:value-of select="name" />
			<xsl:text>, </xsl:text>
			Level:
			<xsl:value-of select="level" />
			<xsl:text>, </xsl:text>
			<xsl:value-of select="years" />
			years
		</p>
	</xsl:template>

</xsl:stylesheet>