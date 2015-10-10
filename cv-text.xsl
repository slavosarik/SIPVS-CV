<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="text" />
<xsl:template match="/">
Biography
=======================================================================
Personal info: <xsl:apply-templates select="biography/personal_info" />
=======================================================================
Contact info: <xsl:apply-templates select="biography/contact_info" />
=======================================================================
Education: <xsl:apply-templates select="biography/education/school"/>
=======================================================================
Courses and certificates: <xsl:apply-templates select="biography/courses/course"/>
=======================================================================
Experience: <xsl:apply-templates select="biography/career/experience"/>
=======================================================================
Skills: <xsl:apply-templates select="biography/skills/skill"/>
=======================================================================
</xsl:template>

<xsl:template match="personal_info">
Full name: <xsl:value-of select="title_before" />
<xsl:text> </xsl:text>
<xsl:value-of select="first_name" />
<xsl:text> </xsl:text>
<xsl:value-of select="surname" />
<xsl:text> </xsl:text>
<xsl:value-of select="title_after" />		
Birthdate: <xsl:apply-templates select="birth_date" />		
</xsl:template>

<xsl:template match="birth_date">	
<xsl:value-of select=" 
 concat(
 substring-after(substring-after(.,'-'),'-') , '.',
 format-number( substring-before(substring-after(.,'-'),'-'), '00') , '.',
 format-number( number( substring-before(.,'-')), '00')
 )
" />
</xsl:template>

<xsl:template match="contact_info">	
Email: <xsl:value-of select="email" />
Phone: <xsl:value-of select="phone" />
Address: <xsl:apply-templates select="address" />
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
Name: <xsl:value-of select="name"/>
Faculty: <xsl:value-of select="faculty"/>
Profession: <xsl:value-of select="profession"/>
Degree: <xsl:value-of select="degree" />
Start date: <xsl:value-of select="start_date" />
End date: <xsl:value-of select="end_date" />
<xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="course">	
Type: <xsl:choose>
<xsl:when test="@type &gt; 0">Certificate</xsl:when>
<xsl:otherwise>Course</xsl:otherwise>
</xsl:choose>
Organization: <xsl:value-of select="organization"/>
Name: <xsl:value-of select="name" />
Valid from: <xsl:value-of select="valid_from" />
Valid to: <xsl:value-of select="valid_to" />
<xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="experience">
Employer: <xsl:value-of select="employer" />	
Profession: <xsl:value-of select="profession" />
Description: <xsl:value-of select="description" />
Start date: <xsl:value-of select="start_date" />
Projects: <xsl:apply-templates select="projects/project"/>
<xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="project">
Name: <xsl:value-of select="name" />
Description: <xsl:value-of select="description" />
Role: <xsl:value-of select="role" />
</xsl:template>	

<xsl:template match="skill">	
Name: <xsl:value-of select="name" />
Level: <xsl:value-of select="level" />
Years: <xsl:value-of select="years" />
<xsl:text>
</xsl:text>
</xsl:template>
	
</xsl:stylesheet>