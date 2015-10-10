<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text"/>
  <xsl:template match="/">
    Biography
    
    Meno a priezvisko: - <xsl:value-of select="/biography/personal_info/first_name"/> <xsl:text> </xsl:text> <xsl:value-of select="/biography/personal_info/surname"/>
    Dátum narodenia: - <xsl:value-of select="/biography/personal_info/birth_date"/>            
  </xsl:template>
</xsl:stylesheet>