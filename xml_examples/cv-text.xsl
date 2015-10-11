<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema">
<xsl:output method="text" />
<xsl:template match="/">
Biography

Personal info: <xsl:apply-templates select="biography/personal_info" />

Contact info: <xsl:apply-templates select="biography/contact_info" />

Education: <xsl:apply-templates select="biography/education/school"/>

Courses and certificates: <xsl:apply-templates select="biography/courses/course"/>

Experience: <xsl:apply-templates select="biography/career/experience"/>

Skills: <xsl:apply-templates select="biography/skills/skill"/>

</xsl:template>

<xsl:template match="personal_info">
<xsl:text>
	</xsl:text>Full name: <xsl:apply-templates select="title_before" />
<xsl:value-of select="first_name" />
<xsl:text> </xsl:text>
<xsl:value-of select="surname" />
<xsl:apply-templates select="title_after" />
<xsl:text>
	</xsl:text>Birth date: <xsl:value-of select="format-date(birth_date,'[D01].[M01].[Y0001]')" />		
</xsl:template>

<xsl:template match="title_before">	
<xsl:if test=". != ''">
<xsl:value-of select="." />	
<xsl:text> </xsl:text>
</xsl:if>
</xsl:template>

<xsl:template match="title_after">
<xsl:if test=". != ''">	
<xsl:text> </xsl:text>
<xsl:value-of select="." />
</xsl:if>	
</xsl:template>

<xsl:template match="contact_info">	
<xsl:text>
	</xsl:text>Email: <xsl:value-of select="email" />
<xsl:text>
	</xsl:text>Phone: <xsl:value-of select="phone" />
<xsl:text>
	</xsl:text>Address: <xsl:apply-templates select="address" />
</xsl:template>

<xsl:template match="address">	
<xsl:value-of select="street"/>
<xsl:text>, </xsl:text>
<xsl:value-of select="postal_code"/>
<xsl:text> </xsl:text>
<xsl:value-of select="city"/>
<xsl:text>, </xsl:text>
<xsl:value-of select="country" />
</xsl:template>

<xsl:template match="school">	
<xsl:text>
	</xsl:text>Name: <xsl:value-of select="name"/>
<xsl:text>
	</xsl:text>Faculty: <xsl:value-of select="faculty"/>
<xsl:text>
	</xsl:text>Profession: <xsl:value-of select="profession"/>
<xsl:text>
	</xsl:text>Degree: <xsl:choose>
<xsl:when test="degree = 'isced_2011_8'">Doctoral or equivalent</xsl:when>
<xsl:when test="degree = 'isced_2011_7'">Master or equivalent</xsl:when>
<xsl:when test="degree = 'isced_2011_6'">Bachelor or equivalent</xsl:when>
<xsl:when test="degree = 'isced_2011_5'">Short-cycle tertiary education</xsl:when>
<xsl:when test="degree = 'isced_2011_4'">Post-secondary non-tertiary education</xsl:when>
<xsl:when test="degree = 'isced_2011_3'">Upper secondary education</xsl:when>
<xsl:when test="degree = 'isced_2011_2'">Lower secondary education</xsl:when>
<xsl:when test="degree = 'isced_2011_1'">Primary education</xsl:when>
</xsl:choose>
<xsl:text>
	</xsl:text>Start date: <xsl:value-of select="format-date(start_date,'[D01].[M01].[Y0001]')" />
<xsl:text>
	</xsl:text>End date: <xsl:value-of select="format-date(end_date,'[D01].[M01].[Y0001]')" />
<xsl:if test="position() != last()">
<xsl:text>
</xsl:text>
</xsl:if>
</xsl:template>

<xsl:template match="course">	
<xsl:text>
	</xsl:text>Type: <xsl:choose>
<xsl:when test="@type = 0">Course</xsl:when>
<xsl:when test="@type = 1">Certificate</xsl:when>
</xsl:choose>
<xsl:text>
	</xsl:text>Organization: <xsl:value-of select="organization"/>
<xsl:text>
	</xsl:text>Name: <xsl:value-of select="name" />
<xsl:text>
	</xsl:text>Valid from: <xsl:value-of select="format-date(valid_from,'[D01].[M01].[Y0001]')" />
<xsl:text>
	</xsl:text>Valid to: <xsl:value-of select="format-date(valid_to,'[D01].[M01].[Y0001]')" />
<xsl:if test="position() != last()">
<xsl:text>
</xsl:text>
</xsl:if>
</xsl:template>

<xsl:template match="experience">
<xsl:text>
	</xsl:text>Employer: <xsl:value-of select="employer" />	
<xsl:text>
	</xsl:text>Profession: <xsl:value-of select="profession" />
<xsl:text>
	</xsl:text>Description: <xsl:value-of select="description" />
<xsl:text>
	</xsl:text>Start date: <xsl:value-of select="format-date(start_date,'[D01].[M01].[Y0001]')" />
<xsl:text>
	</xsl:text>Projects: <xsl:apply-templates select="projects/project"/>
<xsl:if test="position() != last()">
<xsl:text>
</xsl:text>
</xsl:if>
</xsl:template>

<xsl:template match="project">
<xsl:text>
		</xsl:text>Name: <xsl:value-of select="name" />
<xsl:text>
		</xsl:text>Description: <xsl:value-of select="description" />
<xsl:text>
		</xsl:text>Role: <xsl:value-of select="role" />
<xsl:if test="position() != last()">
<xsl:text>
</xsl:text>
</xsl:if>
</xsl:template>	

<xsl:template match="skill">	
<xsl:text>
	</xsl:text>Name: <xsl:value-of select="name" /><xsl:text>, </xsl:text> Level: <xsl:value-of select="level" /> <xsl:text>, </xsl:text> <xsl:value-of select="years" /><xsl:text> years </xsl:text> 
</xsl:template>
	
</xsl:stylesheet>