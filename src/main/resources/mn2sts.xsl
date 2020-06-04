<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mml="http://www.w3.org/1998/Math/MathML" xmlns:tbx="urn:iso:std:iso:30042:ed-1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xalan" version="1.0">

	<xsl:output version="1.0" method="xml" encoding="UTF-8" indent="yes"/>
	
	<xsl:key name="klang" match="*[local-name() = 'bibdata']/*[local-name() = 'title']" use="@language"/>

	<xsl:param name="debug">false</xsl:param>
	<xsl:variable name="change_id">true</xsl:variable>
	
	<xsl:variable name="lower">abcdefghijklmnopqrstuvwxyz</xsl:variable> 
	<xsl:variable name="upper">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
	
	<!-- ====================================================================== -->
	<!-- ====================================================================== -->
	<xsl:variable name="elements">
		<elements>
			<!-- Scope -->
			<xsl:apply-templates select="/*/*[local-name() = 'sections']/*[local-name() = 'clause'][@id = '_scope']" mode="elements"> <!-- [1] -->
				<xsl:with-param name="sectionNum" select="'1'"/>
			</xsl:apply-templates>
			
			<!-- Normative References -->
			<xsl:apply-templates select="/*/*[local-name() = 'bibliography']/*[local-name() = 'references'][@id = '_normative_references']" mode="elements"> <!-- [@id = '_normative_references'] -->
				<xsl:with-param name="sectionNum" select="count(/*/*[local-name() = 'sections']/*[local-name() = 'clause'][@id = '_scope']) + 1"/>
			</xsl:apply-templates>
			
			<!-- Other main sections: Terms, etc... -->					
			<xsl:apply-templates select="/*/*[local-name() = 'sections']/*[@id != '_scope']" mode="elements"> <!-- position() &gt; 1  -->
				<xsl:with-param name="sectionNumSkew" select="'1'"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates select="/*/*[local-name() = 'annex']" mode="elements"/>
			
			<xsl:apply-templates select="/*/*[local-name() = 'bibliography']/*[local-name() = 'references'][@id != '_normative_references']" mode="elements"/>
			
		</elements>
	</xsl:variable>

	<xsl:template match="text()" mode="elements"/>
	
	<xsl:template match="*" mode="elements">
		<xsl:param name="sectionNum"/>
		<xsl:param name="sectionNumSkew" select="0"/>
		<xsl:variable name="sectionNum_">
			<xsl:choose>
				<xsl:when test="$sectionNum"><xsl:value-of select="$sectionNum"/></xsl:when>
				<xsl:when test="$sectionNumSkew != 0">
					<xsl:variable name="number"><xsl:number count="*"/></xsl:variable>
					<xsl:value-of select="$number + $sectionNumSkew"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:number count="*"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
	
		<xsl:variable name="name" select="local-name()"/>

		<xsl:if test="$name = 'annex' or
								$name = 'bibitem' or
								$name = 'clause' or
								$name = 'references' or
								$name = 'terms' or
								$name = 'definitions' or
								$name = 'term' or
								$name = 'preferred' or
								$name = 'admitted' or
								$name = 'deprecates' or
								$name = 'domain' or
								$name = 'em' or
								$name = 'table' or
								$name = 'dl' or
								$name = 'ol' or
								$name = 'ul' or
								$name = 'li' or
								$name = 'figure' or 
								$name = 'image' or
								$name = 'formula' or
								$name = 'stem'">
								
			<xsl:variable name="section_">
				<xsl:call-template name="getSection">
					<xsl:with-param name="sectionNum" select="$sectionNum_"/>
				</xsl:call-template>
			</xsl:variable>
			
			<xsl:variable name="source_id">			
				<xsl:call-template name="getId"/>
			</xsl:variable>
			
			<xsl:variable name="id">
				<xsl:choose>
					<xsl:when test="$change_id = 'false'"><xsl:value-of select="$source_id"/></xsl:when>
					<xsl:otherwise>
						<xsl:choose>
							
							<xsl:when test="$name = 'clause' or 
																			($name = 'references' and @id = '_normative_references') or 
																			$name = 'annex' or
																			$name = 'terms' or
																			$name = 'term' or 
																			$name = 'definitions' or 
																			$name = 'ul' or 
																			$name = 'ol' or 
																			$name = 'li'">sec_</xsl:when>
							<xsl:when test="$name = 'fn'">fn_</xsl:when>
							<xsl:when test="$name = 'preferred' or 
																			$name = 'admitted' or 
																			$name = 'deprecates' or
																			$name = 'domain'">term_</xsl:when>				
							<xsl:when test="$name = 'dl' or $name = 'table'">tab_</xsl:when>
							<xsl:when test="$name = 'figure' or $name = 'image'">fig_</xsl:when>
							<xsl:when test="$name = 'formula'">formula_</xsl:when>
						</xsl:choose>
						<xsl:value-of select="$section_"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			
			<xsl:variable name="section">
				<xsl:choose>
					<xsl:when test="$name = 'annex'">Annex <xsl:value-of select="$section_"/></xsl:when>
					<xsl:when test="$name = 'table'">Table <xsl:value-of select="$section_"/></xsl:when>
					<xsl:otherwise><xsl:value-of select="$section_"/></xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			
			<element source_id="{$source_id}" id="{$id}" section="{$section}" parent="{$name}"/>
			<xsl:if test="$debug = 'true'">
				<xsl:message><xsl:value-of select="$source_id"/></xsl:message>
			</xsl:if>
		</xsl:if>
		
		<xsl:apply-templates mode="elements">
			<xsl:with-param name="sectionNum" select="$sectionNum_"/>
		</xsl:apply-templates>
	</xsl:template>
		
	<xsl:template name="getId">
		<xsl:variable name="name" select="local-name()"/>
		<xsl:choose>
			<xsl:when test="@id"><xsl:value-of select="@id"/></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="generate-id(.)"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
		
	<!-- ====================================================================== -->
	<!-- ====================================================================== -->
	
	
	<xsl:template match="@*|node()">
		<xsl:param name="sectionNum"/>
		<xsl:param name="sectionNumSkew"/>
		<xsl:copy>
				<xsl:apply-templates select="@*|node()">
					<xsl:with-param name="sectionNum" select="$sectionNum"/>
					<xsl:with-param name="sectionNumSkew" select="$sectionNumSkew"/>
				</xsl:apply-templates>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="*">
		<xsl:param name="sectionNum"/>
		<xsl:param name="sectionNumSkew"/>
		<xsl:element name="{local-name()}">
			<xsl:apply-templates select="@*|node()">
				<xsl:with-param name="sectionNum" select="$sectionNum"/>
				<xsl:with-param name="sectionNumSkew" select="$sectionNumSkew"/>
			</xsl:apply-templates>
		</xsl:element>
	</xsl:template>
	
	<!-- roor element, for example: iso-standard -->
	<xsl:template match="/*">
		
		<standard xmlns:mml="http://www.w3.org/1998/Math/MathML" xmlns:tbx="urn:iso:std:iso:30042:ed-1" xmlns:xlink="http://www.w3.org/1999/xlink">
			
			<front>
				<xsl:apply-templates select="*[local-name() = 'bibdata']" mode="front"/>
				<xsl:apply-templates select="*[local-name() = 'preface']" mode="front"/>
			</front>
			
			
			<xsl:if test="*[local-name() = 'sections'] or *[local-name() = 'bibliography']/*[local-name() = 'references'][@id = '_normative_references']">
				<body>					
					<!-- Scope -->
					<xsl:apply-templates select="*[local-name() = 'sections']/*[local-name() = 'clause'][@id = '_scope']"> <!-- [1] -->
						<xsl:with-param name="sectionNum" select="'1'"/>
					</xsl:apply-templates>
					
					<!-- Normative References -->
					<xsl:apply-templates select="*[local-name() = 'bibliography']/*[local-name() = 'references'][@id = '_normative_references']"> <!-- [@id = '_normative_references'] -->
						<xsl:with-param name="sectionNum" select="count(*[local-name() = 'sections']/*[local-name() = 'clause'][@id = '_scope']) + 1"/>
					</xsl:apply-templates>
					
					<!-- Other main sections: Terms, etc... -->					
					<xsl:apply-templates select="*[local-name() = 'sections']/*[@id != '_scope']"> <!-- position() &gt; 1  -->
						<xsl:with-param name="sectionNumSkew" select="'1'"/>
					</xsl:apply-templates>
					
				</body>	
			</xsl:if>
						
			<xsl:if test="*[local-name() = 'annex'] or *[local-name() = 'bibliography']/*[local-name() = 'references']">
				<back>
					<xsl:if test="*[local-name() = 'annex']">
						<app-group>
							<xsl:apply-templates select="*[local-name() = 'annex']" mode="back"/>
						</app-group>
					</xsl:if>
					<xsl:apply-templates select="*[local-name() = 'bibliography']/*[local-name() = 'references'][@id != '_normative_references']" mode="back"/>
				</back>
			</xsl:if>
			<xsl:if test="$debug = 'true'">
				<xsl:text disable-output-escaping="yes">&lt;!-- </xsl:text>
				<xsl:copy-of select="xalan:nodeset($elements)"/>
				<xsl:text disable-output-escaping="yes">--&gt;</xsl:text>
			</xsl:if>
		</standard>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'bibdata']"/>
	<xsl:template match="*[local-name() = 'preface']"/>
	<xsl:template match="*[local-name() = 'annex']"/>
	<xsl:template match="*[local-name() = 'bibliography']"/>
	
	<xsl:template match="*[local-name() = 'bibdata']" mode="front">
		<iso-meta>
			<xsl:for-each select="*[local-name() = 'title'][generate-id(.)=generate-id(key('klang',@language)[1])]">
				<title-wrap xml:lang="{@language}">
					<xsl:variable name="title-intro" select="/*/*[local-name() = 'bibdata']/*[local-name() = 'title'][@language = current()/@language and @type = 'title-intro']"/>
					<intro><xsl:value-of select="$title-intro"/></intro>
					<xsl:variable name="title-main" select="/*/*[local-name() = 'bibdata']/*[local-name() = 'title'][@language = current()/@language and @type = 'title-main']"/>
					<main><xsl:value-of select="$title-main"/></main>
					<xsl:variable name="title-part" select="/*/*[local-name() = 'bibdata']/*[local-name() = 'title'][@language = current()/@language and @type = 'title-part']"/>
					<compl><xsl:value-of select="$title-part"/></compl>
					
					<xsl:variable name="part" select="/*/*[local-name() = 'bibdata']/*[local-name() = 'ext']/*[local-name() = 'structuredidentifier']/*[local-name() = 'project-number']/@part"/>
					
					<xsl:variable name="full-title">
						<xsl:if test="normalize-space($title-intro) != ''">
							<xsl:value-of select="$title-intro"/>
							<xsl:text> — </xsl:text>
						</xsl:if>
						<xsl:value-of select="$title-main"/>
						<xsl:if test="normalize-space($title-part) != ''">
							<xsl:if test="$part != ''">
								<xsl:text> — </xsl:text>															
									<xsl:choose>
										<xsl:when test="current()/@language = 'en'"><xsl:text>Part </xsl:text></xsl:when>
										<xsl:when test="current()/@language = 'fr'"><xsl:text>Partie </xsl:text></xsl:when>
									</xsl:choose>
									<xsl:value-of select="$part"/>
									<xsl:text>: </xsl:text>															
							</xsl:if>
							<xsl:value-of select="$title-part"/>
						</xsl:if>
					</xsl:variable>
					<full><xsl:value-of select="$full-title"/></full>
				</title-wrap>
			</xsl:for-each>
			<doc-ident>
				<sdo><xsl:value-of select="*[local-name() = 'contributor'][*[local-name() = 'role']/@type='author']/*[local-name() = 'organization']/*[local-name() = 'abbreviation']"/></sdo>
				<proj-id><xsl:value-of select="*[local-name() = 'ext']/*[local-name() = 'structuredidentifier']/*[local-name() = 'project-number']"/></proj-id>
				<language><xsl:value-of select="*[local-name() = 'language']"/></language>
				<release-version><xsl:value-of select="*[local-name() = 'status']/*[local-name() = 'stage']/@abbreviation"/></release-version>
			</doc-ident>
			<std-ident>
				<originator><xsl:value-of select="*[local-name() = 'contributor'][*[local-name() = 'role']/@type='publisher']/*[local-name() = 'organization']/*[local-name() = 'abbreviation']"/></originator>
				<doc-type><xsl:value-of select="*[local-name() = 'ext']/*[local-name() = 'doctype']"/></doc-type>
				<doc-number><xsl:value-of select="*[local-name() = 'docnumber']"/></doc-number>
				<part-number><xsl:value-of select="*[local-name() = 'ext']/*[local-name() = 'structuredidentifier']/*[local-name() = 'partnumber']"/></part-number>
				<edition><xsl:value-of select="*[local-name() = 'edition']"/></edition>
				<version><xsl:value-of select="*[local-name() = 'version']/*[local-name() = 'revision-date']"/></version>
			</std-ident>
			<content-language><xsl:value-of select="*[local-name() = 'language']"/></content-language>
			<std-ref type="dated"><xsl:value-of select="*[local-name() = 'docidentifier']"/></std-ref>
			<std-ref type="undated"><xsl:value-of select="substring-before(*[local-name() = 'docidentifier'], ':')"/></std-ref>
			<doc-ref><xsl:value-of select="*[local-name() = 'docidentifier'][@type='iso-with-lang']"/></doc-ref>
			<release-date><xsl:value-of select="*[local-name() = 'date'][@type='published']/*[local-name() = 'on']"/></release-date>
			<comm-ref><xsl:value-of select="concat(*[local-name() = 'copyright']/*[local-name() = 'owner']/*[local-name() = 'organization']/*[local-name() = 'abbreviation'], 
										'/', *[local-name() = 'ext']/*[local-name() = 'editorialgroup']/*[local-name() = 'technical-committee']/@type, ' ',
										*[local-name() = 'ext']/*[local-name() = 'editorialgroup']/*[local-name() = 'technical-committee']/@number)"/>
			</comm-ref>
			<secretariat><xsl:value-of select="*[local-name() = 'ext']/*[local-name() = 'editorialgroup']/*[local-name() = 'secretariat']"/></secretariat>				
			<ics><xsl:value-of select="*[local-name() = 'ext']/*[local-name() = 'editorialgroup']/*[local-name() = 'ics']/*[local-name() = 'code']"/></ics>
			<permissions>
				<copyright-statement>All rights reserved</copyright-statement>
				<copyright-year><xsl:value-of select="*[local-name() = 'copyright']/*[local-name() = 'from']"/></copyright-year>
				<copyright-holder><xsl:value-of select="*[local-name() = 'copyright']/*[local-name() = 'owner']/*[local-name() = 'organization']/*[local-name() = 'abbreviation']"/></copyright-holder>
			</permissions>
		</iso-meta>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'preface']/*" mode="front">
		<xsl:variable name="name" select="local-name()"/>
		<xsl:variable name="sec_type">
			<xsl:choose>
				<xsl:when test="$name = 'introduction'">intro</xsl:when>
				<xsl:otherwise><xsl:value-of select="$name"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<sec id="sec_{$sec_type}" sec-type="{$sec_type}">
			<xsl:apply-templates />
		</sec>
	</xsl:template>
	
	
	<xsl:template match="*[local-name() = 'annex']" mode="back">
		<xsl:variable name="current_id">
			<xsl:call-template name="getId"/>
		</xsl:variable>
		<xsl:variable name="id" select="xalan:nodeset($elements)//element[@source_id = $current_id]/@id"/>
		<xsl:variable name="section" select="xalan:nodeset($elements)//element[@source_id = $current_id]/@section"/>
		<app content-type="inform-annex" id="{$id}">
			<xsl:attribute name="content-type">
				<xsl:choose>
					<xsl:when test="@obligation   = 'informative'">inform-annex</xsl:when>
					<xsl:otherwise><xsl:value-of select="@obligation"/></xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
			<label><xsl:value-of select="$section"/></label>
			<annex-type>(<xsl:value-of select="@obligation"/>)</annex-type>
			<xsl:apply-templates />
		</app>
	</xsl:template>
	
	
	<xsl:template match="*[local-name() = 'bibliography']/*[local-name() = 'references'][@id != '_normative_references']" mode="back">
		<ref-list content-type="bibl" id="sec_bibl">
			<xsl:apply-templates/>
		</ref-list>
	</xsl:template>
	
	
	<xsl:template match="*[local-name() = 'bibitem'][1][ancestor::*[local-name() = 'references'][@id = '_normative_references']]" priority="2">
		<ref-list content-type="norm-refs">
			<xsl:for-each select="../*[local-name() = 'bibitem']">
				<xsl:call-template name="bibitem"/>
			</xsl:for-each>
		</ref-list>
	</xsl:template>
	<xsl:template match="*[local-name() = 'bibitem'][position() &gt; 1][ancestor::*[local-name() = 'references'][@id = '_normative_references']]" priority="2"/>
	
	<xsl:template match="*[local-name() = 'bibitem']" name="bibitem">
		<xsl:variable name="current_id">
			<xsl:call-template name="getId"/>
		</xsl:variable>
		<xsl:variable name="id" select="xalan:nodeset($elements)//element[@source_id = $current_id]/@id"/>
		
		<ref>
			<xsl:if test="@type">
				<xsl:attribute name="content-type">
					<xsl:value-of select="@type"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
			<label><xsl:number format="[1]"/></label>
			<std>
				<std-ref><xsl:value-of select="*[local-name() = 'docidentifier']"/></std-ref>
				<xsl:apply-templates select="*[local-name() = 'note']"/>				
				<xsl:apply-templates select="*[local-name() = 'title'][(@type = 'main' and @language = 'en') or not(@type and @language)]"/>				
				<xsl:apply-templates select="*[local-name() = 'formattedref']"/>
			</std>
			
		</ref>
		
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'bibitem']/*[local-name() = 'title']">
		<xsl:text>, </xsl:text>
		<title><xsl:apply-templates/></title>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'bibitem']/*[local-name() = 'formattedref']">
		<title><xsl:apply-templates/></title>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'formattedref']/*[local-name() = 'em']" priority="2">
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'bibitem']/*[local-name() = 'note'] | *[local-name() = 'fn'] " priority="2">
		<xsl:variable name="number">
			<xsl:number level="any" count="*[local-name() = 'bibitem']/*[local-name() = 'note'] | *[local-name() = 'fn']"/>
		</xsl:variable>
		
		<xsl:variable name="xref_fn">
			<xref ref-type="fn" rid="fn_{$number}">
				<sup><xsl:value-of select="$number"/></sup>
			</xref>
			<fn id="fn_{$number}">
				<label>
					<sup><xsl:value-of select="$number"/></sup>
				</label>
				<xsl:choose>
					<xsl:when test="local-name() = 'fn'">
						<xsl:apply-templates/>
					</xsl:when>
					<xsl:otherwise>
						<p><xsl:apply-templates/></p>
					</xsl:otherwise>
				</xsl:choose>
			</fn>
		</xsl:variable>
		
		<xsl:choose>		
			<xsl:when test="preceding-sibling::*[1][local-name() = 'image']">
				<!-- enclose in 'p' -->
				<p>
					<xsl:copy-of select="$xref_fn"/>
				</p>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="$xref_fn"/>
			</xsl:otherwise>
		</xsl:choose>		
		
	</xsl:template>
	
	
	<xsl:template match="*[local-name() = 'clause'] | 
																*[local-name() = 'references'][@id = '_normative_references'] | 
																*[local-name() = 'terms'] |
																*[local-name() = 'definitions']">
		<xsl:param name="sectionNum"/>
		<xsl:param name="sectionNumSkew" select="0"/>
		<xsl:variable name="sectionNum_">
			<xsl:choose>
				<xsl:when test="$sectionNum"><xsl:value-of select="$sectionNum"/></xsl:when>
				<xsl:when test="$sectionNumSkew != 0">
					<xsl:variable name="number"><xsl:number count="*"/></xsl:variable>
					<xsl:value-of select="$number + $sectionNumSkew"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:number count="*"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
	
		<xsl:variable name="sec_type">
			<xsl:choose>
				<xsl:when test="@id = '_scope'">scope</xsl:when>
				<xsl:when test="@id = '_normative_references'">norm-refs</xsl:when>
				<xsl:when test="@id = 'tda' or @id = 'terms'">terms</xsl:when>
				<xsl:otherwise><!-- <xsl:value-of select="@id"/> --></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
	
		<xsl:variable name="current_id">
			<xsl:call-template name="getId"/>
		</xsl:variable>
		
		<xsl:variable name="id" select="xalan:nodeset($elements)//element[@source_id = $current_id]/@id"/>	
		<xsl:variable name="section" select="xalan:nodeset($elements)//element[@source_id = $current_id]/@section"/>	
		
		<sec id="{$id}">
			<xsl:if test="normalize-space($sec_type) != ''">
				<xsl:attribute name="sec-type">
					<xsl:value-of select="$sec_type"/>
				</xsl:attribute>
			</xsl:if>
			<label><xsl:value-of select="$section"/></label>			
			<xsl:apply-templates>
				<xsl:with-param name="sectionNum" select="$sectionNum_"/>
			</xsl:apply-templates>
		</sec>
	</xsl:template>


	<xsl:template match="*[local-name() = 'term']">
		<xsl:param name="sectionNum"/>
		<xsl:variable name="current_id">
			<xsl:call-template name="getId"/>
		</xsl:variable>
		<xsl:variable name="id" select="xalan:nodeset($elements)//element[@source_id = $current_id]/@id"/>	
		<xsl:variable name="section" select="xalan:nodeset($elements)//element[@source_id = $current_id]/@section"/>			
		<term-sec id="{$id}">
			<label><xsl:value-of select="$section"/></label>
			<tbx:termEntry id="term_{$section}">
				<tbx:langSet xml:lang="en">
					<xsl:apply-templates>
						<xsl:with-param name="sectionNum" select="$sectionNum"/>
					</xsl:apply-templates>
					<xsl:apply-templates select="*[local-name() = 'termexample']" mode="termEntry">
						<xsl:with-param name="sectionNum" select="$sectionNum"/>
					</xsl:apply-templates>
					<xsl:apply-templates select="*[local-name() = 'termnote']" mode="termEntry">
						<xsl:with-param name="sectionNum" select="$sectionNum"/>
					</xsl:apply-templates>
					<xsl:apply-templates select="*[local-name() = 'termsource']" mode="termEntry">
						<xsl:with-param name="sectionNum" select="$sectionNum"/>
					</xsl:apply-templates>
					
					<xsl:apply-templates select="*[local-name() = 'preferred'] | *[local-name() = 'admitted'] | *[local-name() = 'deprecates'] | *[local-name() = 'domain']" mode="termEntry">
						<xsl:with-param name="sectionNum" select="$sectionNum"/>
					</xsl:apply-templates>
					
				</tbx:langSet>
			</tbx:termEntry>
		</term-sec>
	</xsl:template>	

	<xsl:template match="*[local-name() = 'definition']">
		<tbx:definition>
			<xsl:apply-templates />
		</tbx:definition>
	</xsl:template>

	<xsl:template match="*[local-name() = 'termexample']" mode="termEntry">
		<tbx:example>
			<xsl:apply-templates />
		</tbx:example>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'definition']/text()[1] |
																*[local-name() = 'termexample']/text()[1] | 
																*[local-name() = 'termnote']/text()[1] |
																*[local-name() = 'termsource']/text()[1] |
																*[local-name() = 'modification']/text()[1] |
																*[local-name() = 'dd']/text()[1] |
																*[local-name() = 'formattedref']/text()[1]">
		<xsl:value-of select="normalize-space()"/>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'termnote']" mode="termEntry">
		<tbx:note>
			<xsl:apply-templates />
		</tbx:note>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'termsource']" mode="termEntry">		
		<tbx:source>
			<xsl:value-of select="*[local-name() = 'origin']/@citeas"/>
			<xsl:apply-templates select="*[local-name() = 'origin']/*[local-name() = 'localityStack']"/>
			<xsl:apply-templates select="*[local-name() = 'modification']"/>
		</tbx:source>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'localityStack']">
		<xsl:text>, </xsl:text>
		<xsl:for-each select="*[local-name() = 'locality']">
			<xsl:value-of select="*[local-name() = 'referenceFrom']"/>
			<xsl:if test="position() != last()">; </xsl:if>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'modification']">
		<xsl:text>, modified — </xsl:text>
		<xsl:apply-templates />
	</xsl:template>
	
	
	<xsl:template match="*[local-name() = 'termexample'] | *[local-name() = 'termnote'] | *[local-name() = 'termsource']"/>
	
	<xsl:template match="*[local-name() = 'preferred'] | *[local-name() = 'admitted'] | *[local-name() = 'deprecates'] | *[local-name() = 'domain']"/>
	<xsl:template match="*[local-name() = 'preferred'] | *[local-name() = 'admitted'] | *[local-name() = 'deprecates'] | *[local-name() = 'domain']" mode="termEntry">
		<xsl:param name="sectionNum"/>
		
		<xsl:variable name="current_id">
			<xsl:call-template name="getId"/>
		</xsl:variable>
		
		<xsl:variable name="id" select="xalan:nodeset($elements)//element[@source_id = $current_id]/@id"/>	
		
		<tbx:tig id="{$id}">
			<tbx:term><xsl:apply-templates /></tbx:term>
			<tbx:partOfSpeech value="noun"/>
		</tbx:tig>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'p']">
		<xsl:choose>
			<xsl:when test="parent::*[local-name() = 'termexample'] or 
														parent::*[local-name() = 'definition']  or 
														parent::*[local-name() = 'termnote'] or 
														parent::*[local-name() = 'modification'] or
														parent::*[local-name() = 'dd']">
				<xsl:apply-templates />
			</xsl:when>
			<xsl:otherwise>
				<p id="{@id}">
					<xsl:apply-templates />
				</p>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
	<xsl:template match="*[local-name() = 'ul']">
		<list list-type="bullet"> <!-- even when <label>—</label> ! -->
			<xsl:apply-templates />
		</list>
		<xsl:for-each select="*[local-name() = 'note']">
			<xsl:call-template name="note"/>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'ol']">
		<list>
			<xsl:attribute name="list-type">
				<xsl:choose>
					<xsl:when test="@type = 'arabic'">alpha-lower</xsl:when>
					<xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
			<xsl:apply-templates />
		</list>
		<xsl:for-each select="*[local-name() = 'note']">
			<xsl:call-template name="note"/>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="*[local-name() = 'ul']/*[local-name() = 'note'] | *[local-name() = 'ol']/*[local-name() = 'note']" priority="2"/>
	
	<xsl:template match="*[local-name() = 'li']">
		<list-item>
			<xsl:if test="@id">
				<xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="local-name(..) = 'ul'"><label>—</label></xsl:when>
				<xsl:when test="local-name(..) = 'ol'">
					<xsl:variable name="type" select="parent::*/@type"/>
					<xsl:choose>
						<xsl:when test="$type = 'arabic'">
							<label><xsl:number format="a)"/></label>
						</xsl:when>
						<xsl:otherwise>
							<label><xsl:number format="a)"/></label>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
			</xsl:choose>
			<xsl:apply-templates />
		</list-item>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'example']">
		<xsl:variable name="clause_id" select="ancestor::*[local-name() = 'clause'][1]/@id"/>		
		<non-normative-example>
			<label>
				<xsl:text>EXAMPLE</xsl:text>
				<xsl:if test="count(ancestor::*[local-name() = 'clause'][1]//*[local-name() = 'example']) &gt; 1">
					<xsl:text> </xsl:text><xsl:number level="any" count="*[local-name() = 'example'][ancestor::*[local-name() = 'clause'][@id = $clause_id]]"/>
				</xsl:if>
			</label>
			<xsl:apply-templates/>
		</non-normative-example>
	</xsl:template>
	
	
	
	<xsl:template match="*[local-name() = 'note']" name="note">
		<xsl:variable name="clause_id" select="ancestor::*[local-name() = 'clause'][1]/@id"/>		
		<non-normative-note>
			<label>
				<xsl:text>NOTE</xsl:text>
				<xsl:if test="count(ancestor::*[local-name() = 'clause'][1]//*[local-name() = 'note']) &gt; 1">
					<xsl:text> </xsl:text><xsl:number level="any" count="*[local-name() = 'note'][ancestor::*[local-name() = 'clause'][@id = $clause_id]]"/>
				</xsl:if>
			</label>
			<xsl:apply-templates/>
		</non-normative-note>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'eref']">
		<std>
			<std-ref><xsl:value-of select="@citeas"/></std-ref>
			<xsl:apply-templates select="*[local-name() = 'localityStack']"/>
		</std>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'link']">
		<uri><xsl:value-of select="@target"/></uri>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'strong']">
		<bold><xsl:apply-templates /></bold>
	</xsl:template>

	<!-- <xsl:template match="*[local-name() = 'definition']//*[local-name() = 'em']" priority="2"> -->
	<!-- for em, when next is xref -->
	<xsl:template match="*[local-name() = 'em'][following-sibling::*[1][local-name() = 'xref']]" priority="2">
		<tbx:entailedTerm>
			<xsl:variable name="target" select="following-sibling::*[1]/@target"/>					
			<xsl:variable name="section" select="xalan:nodeset($elements)//element[@source_id = $target]/@section"/>
			<xsl:attribute name="target">
				<xsl:text>term_</xsl:text><xsl:value-of select="$section"/>
			</xsl:attribute>
			<xsl:apply-templates />
		</tbx:entailedTerm>
	</xsl:template>
	

	<!-- for xref, when previous is em -->
	<xsl:template match="*[local-name() = 'xref'][preceding-sibling::*[1][local-name() = 'em']]" priority="2">
		<xsl:value-of select="xalan:nodeset($elements)//element[@source_id = current()/@target]/@section"/>	
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'em']">
		<italic><xsl:apply-templates /></italic>
	</xsl:template>

	<xsl:template match="*[local-name() = 'br']">
		<break/>
	</xsl:template>
	
	
	<xsl:template match="*[local-name() = 'th']/*[local-name() = 'br']">
		<xsl:text disable-output-escaping="yes">&lt;/bold&gt;</xsl:text>
		<break/>
		<xsl:text disable-output-escaping="yes">&lt;bold&gt;</xsl:text>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'xref']">
		<xsl:variable name="section" select="xalan:nodeset($elements)//element[@source_id = current()/@target]/@section"/>
		<xsl:variable name="id" select="xalan:nodeset($elements)//element[@source_id = current()/@target]/@id"/>
		<xsl:variable name="parent" select="xalan:nodeset($elements)//element[@source_id = current()/@target]/@parent"/>
		<xsl:variable name="ref_type">
			<xsl:choose>
				<xsl:when test="$parent = 'table'">table</xsl:when>
				<xsl:when test="$parent = 'annex'">app</xsl:when>
				<xsl:when test="$parent = 'fn'">fn</xsl:when>
				<xsl:otherwise>sec</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
	
		<xref ref-type="{$ref_type}" rid="{$id}">
			<xsl:if test="normalize-space($id) = ''">
				<xsl:attribute name="rid"><xsl:value-of select="@target"/></xsl:attribute>
			</xsl:if>
			<xsl:value-of select="$section"/>
		</xref>
		
	</xsl:template>
	
	<!-- need to be tested (find original NISO) -->
	<xsl:template match="*[local-name() = 'callout']">
		<xref ref-type="other" rid="{@target}">
			<xsl:apply-templates/>
		</xref>
	</xsl:template>
	
	<!-- https://github.com/metanorma/mn2sts/issues/8 -->
	<xsl:template match="*[local-name() = 'admonition']">
		<non-normative-note id="{@id}">
			<label><xsl:value-of select="translate(@type, $lower, $upper)"/></label>
			<xsl:apply-templates />
		</non-normative-note>
	</xsl:template>
	
	
	<!-- https://github.com/metanorma/mn2sts/issues/9 -->
	<xsl:template match="*[local-name() = 'quote']">
		<disp-quote id="{@id}">
			<xsl:apply-templates select="*[local-name() = 'p']"/>
			<xsl:if test="*[local-name() = 'source'] or *[local-name() = 'author']">
				<related-object>
					<xsl:apply-templates select="*[local-name() = 'author']" mode="disp-quote"/>
					<xsl:if test="*[local-name() = 'source'] and *[local-name() = 'author']">, </xsl:if>
					<xsl:apply-templates select="*[local-name() = 'source']" mode="disp-quote"/>
				</related-object>
			</xsl:if>
		</disp-quote>
	</xsl:template>	
	<xsl:template match="*[local-name() = 'quote']/*[local-name() = 'source']"/>
	<xsl:template match="*[local-name() = 'quote']/*[local-name() = 'author']"/>
	
	<xsl:template match="*[local-name() = 'quote']/*[local-name() = 'author']" mode="disp-quote">
		<xsl:apply-templates />
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'quote']/*[local-name() = 'source']" mode="disp-quote">
		<xsl:value-of select="@citeas"/>
		<xsl:apply-templates mode="disp-quote"/>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'localityStack']" mode="disp-quote">		
		<xsl:for-each select="*[local-name()='locality']">
			<xsl:if test="position() =1"><xsl:text>, </xsl:text></xsl:if>
			<xsl:apply-templates select="." mode="disp-quote"/>
			<xsl:if test="position() != last()"><xsl:text>; </xsl:text></xsl:if>
		</xsl:for-each>	
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'locality']"  mode="disp-quote">
		<xsl:choose>
			<xsl:when test="@type ='clause'">Clause </xsl:when>
			<xsl:when test="@type ='annex'">Annex </xsl:when>
			<xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
		</xsl:choose>
		<xsl:value-of select="*[local-name() = 'referenceFrom']"/>
	</xsl:template>
	

	<!-- https://github.com/metanorma/mn2sts/issues/10 -->
	<xsl:template match="*[local-name() = 'appendix']">
		<sec id="{@id}" sec-type="appendix">
			<xsl:apply-templates/>
		</sec>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'annotation']">
		<element-citation>
			<annotation id="{@id}">
				<xsl:apply-templates/>
			</annotation>
		</element-citation>
	</xsl:template>
	
	
	
	<xsl:template match="*[local-name() = 'table']"> <!-- [*[local-name() = 'name']] -->
		<xsl:variable name="number"><xsl:number level="any"/></xsl:variable>
		<xsl:variable name="current_id">
			<xsl:call-template name="getId"/>
		</xsl:variable>
		<xsl:variable name="id" select="xalan:nodeset($elements)//element[@source_id = $current_id]/@id"/>
		<xsl:variable name="section" select="xalan:nodeset($elements)//element[@source_id = $current_id]/@section"/>
		<table-wrap id="{$id}" position="float">
			<label><xsl:value-of select="$section"/></label>				
			<xsl:apply-templates select="*[local-name() = 'name']" mode="table"/>				
			<table width="100%">
				<xsl:apply-templates select="*[local-name() = 'thead']" mode="table"/>
				<xsl:apply-templates select="*[local-name() = 'tfoot']" mode="table"/>
				<xsl:apply-templates select="*[local-name() = 'tbody']" mode="table"/>
				<xsl:apply-templates />
			</table>			
		</table-wrap>
		<!-- move notes outside table -->
		<xsl:for-each select="*[local-name() = 'note']">
			<xsl:call-template name="note"/>
		</xsl:for-each>
	</xsl:template>

	
	
	<xsl:template match="*[local-name() = 'table']/*[local-name() = 'note']" priority="2"/>
	
	<xsl:template match="*[local-name() = 'name']"/>
	<xsl:template match="*[local-name() = 'name']" mode="table">
		<caption>
			<title>
				<xsl:apply-templates/>
			</title>
		</caption>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'thead'] | *[local-name() = 'tfoot'] | *[local-name() = 'tbody']"/>
	<xsl:template match="*[local-name() = 'thead'] | *[local-name() = 'tfoot'] | *[local-name() = 'tbody']" mode="table">
		<xsl:element name="{local-name()}">
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates />
		</xsl:element>	
	</xsl:template>
	
	
	<xsl:template match="*[local-name() = 'th']">
		<th>
			<xsl:apply-templates select="@*"/>
			<bold>
				<xsl:apply-templates />
			</bold>
		</th>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'dl']">
		<xsl:variable name="current_id">
			<xsl:call-template name="getId"/>
		</xsl:variable>
		<xsl:variable name="id" select="xalan:nodeset($elements)//element[@source_id = $current_id]/@id"/>
		<p>
		<array id="{$id}">
			<table>
				<tbody>
					<xsl:apply-templates />
				</tbody>
			</table>
		</array>
		</p>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'dt']">
		<tr>
			<td align="left" scope="row" valign="top"><xsl:apply-templates/></td>
			<xsl:apply-templates select="following-sibling::*[local-name() = 'dd'][1]" mode="array"/>
			
		</tr>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'dd']"/>
	<xsl:template match="*[local-name() = 'dd']" mode="array">
		<td align="left" valign="top"><xsl:apply-templates/></td>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'figure'][*[local-name() = 'figure']]" priority="1">
		<xsl:variable name="current_id">
			<xsl:call-template name="getId"/>
		</xsl:variable>
		<xsl:variable name="id" select="xalan:nodeset($elements)//element[@source_id = $current_id]/@id"/>
		<xsl:variable name="section" select="xalan:nodeset($elements)//element[@source_id = $current_id]/@section"/>
		<fig-group content-type="figures" id="{$id}">
			<label><xsl:value-of select="$section"/></label>
			<xsl:apply-templates />
		</fig-group>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'figure']">
		<xsl:variable name="current_id">
			<xsl:call-template name="getId"/>
		</xsl:variable>
		<xsl:variable name="id" select="xalan:nodeset($elements)//element[@source_id = $current_id]/@id"/>
		<xsl:variable name="section" select="xalan:nodeset($elements)//element[@source_id = $current_id]/@section"/>
		<fig fig-type="figure" id="{$id}">
			<label><xsl:value-of select="$section"/></label>
			<xsl:apply-templates />
		</fig>
	</xsl:template>
	
	
	<xsl:template match="*[local-name() = 'figure']/*[local-name() = 'name']">
		<caption>
			<title>
				<xsl:apply-templates/>
			</title>
		</caption>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'figure']/*[local-name() = 'image']">
		<xsl:variable name="current_id">
			<xsl:call-template name="getId"/>
		</xsl:variable>
		<xsl:variable name="id" select="xalan:nodeset($elements)//element[@source_id = $current_id]/@id"/>
		<!-- NISO STS TagLibrary: https://www.niso-sts.org/TagLibrary/niso-sts-TL-1-0-html/element/graphic.html -->
		<graphic id="{$id}" xlink:href="{$id}">
			<!-- <xsl:copy-of select="@mimetype"/> -->
			<xsl:apply-templates select="@*"/>
			<!-- <xsl:processing-instruction name="isoimg-id">
				<xsl:value-of select="@src"/>
			</xsl:processing-instruction> -->
			
		</graphic>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'image']/@src">
		<xsl:attribute name="xlink:href">
			<xsl:value-of select="."/>
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'image']/@mimetype">
		<xsl:copy-of select="."/>
	</xsl:template> <!-- created image processing -->
	<xsl:template match="*[local-name() = 'image']/@alt">
		<xsl:element name="alt-text">
			<xsl:value-of select="."/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="*[local-name() = 'image']/@height"/>	
	<xsl:template match="*[local-name() = 'image']/@width"/>	
	
	
	<xsl:template match="*[local-name() = 'formula']">
		<xsl:apply-templates />
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'stem']">
		<xsl:if test="parent::*[local-name() = 'th']">
			<xsl:text disable-output-escaping="yes">&lt;/bold&gt;</xsl:text>
		</xsl:if>
		<disp-formula>
			<xsl:if test="parent::*[local-name() = 'formula']">
				<xsl:variable name="current_id" select="../@id"/>		
				<xsl:variable name="id" select="xalan:nodeset($elements)//element[@source_id = $current_id]/@id"/>
				<xsl:attribute name="id">
					<xsl:value-of select="$id"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates />
		</disp-formula>
		<xsl:if test="parent::*[local-name() = 'th']">
			<xsl:text disable-output-escaping="yes">&lt;bold&gt;</xsl:text>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="mml:*">
		<xsl:element name="mml:{local-name()}">		
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'tt']">
		<monospace>
			<xsl:apply-templates/>
		</monospace>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'smallcap']">
		<sc>
			<xsl:apply-templates/>
		</sc>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'review']"/>

	<xsl:template match="*[local-name() = 'sourcecode']">
		<code>
			<xsl:apply-templates/>
		</code>
	</xsl:template>
		
	
	<xsl:template name="getLevel">
		<xsl:variable name="level_total" select="count(ancestor::*)"/>
		<xsl:variable name="level">
			<xsl:choose>
				<xsl:when test="ancestor::*[local-name() = 'preface']">
					<xsl:value-of select="$level_total - 1"/>
				</xsl:when>
				<xsl:when test="ancestor::*[local-name() = 'sections']">
					<xsl:value-of select="$level_total - 1"/>
				</xsl:when>
				<xsl:when test="ancestor::*[local-name() = 'bibliography']">
					<xsl:value-of select="$level_total - 1"/>
				</xsl:when>
				<xsl:when test="local-name(ancestor::*[1]) = 'annex'">2</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$level_total"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:value-of select="$level"/>
	</xsl:template>
	
	<xsl:template name="getSection">
		<xsl:param name="sectionNum"/>
		<xsl:variable name="level">
			<xsl:call-template name="getLevel"/>
		</xsl:variable>		
		<xsl:variable name="section">
			<xsl:choose>
				<xsl:when test="local-name() = 'dl'"><xsl:number format="a" level="any"/></xsl:when>
				<xsl:when test="local-name() = 'bibitem' and ancestor::*[local-name() = 'references'][@id='_normative_references']">norm_ref_<xsl:number/></xsl:when>
				<xsl:when test="local-name() = 'bibitem'">ref_<xsl:number/></xsl:when>
				<xsl:when test="ancestor::*[local-name() = 'bibliography']">
					<xsl:value-of select="$sectionNum"/>
				</xsl:when>
				<xsl:when test="local-name() = 'annex'">
					<xsl:number format="A" level="any" count="*[local-name() = 'annex']"/>
				</xsl:when>
				<xsl:when test="ancestor::*[local-name() = 'sections']">
					<!-- 1, 2, 3, 4, ... from main section (not annex, bibliography, ...) -->
					<xsl:choose>
						<xsl:when test="local-name() = 'table'"><xsl:number format="1" level="any" count="*[local-name() = 'sections']//*[local-name() = 'table']"/></xsl:when>
						<xsl:when test="$level = 1">
							<xsl:value-of select="$sectionNum"/>
						</xsl:when>
						<xsl:when test="$level &gt;= 2">
							<xsl:variable name="num">
								<xsl:number format=".1" level="multiple" count="*[local-name() = 'clause']/*[local-name() = 'clause'] | 
																																										*[local-name() = 'clause']/*[local-name() = 'terms'] | 
																																										*[local-name() = 'terms']/*[local-name() = 'term'] | 
																																										*[local-name() = 'clause']/*[local-name() = 'term'] |  
																																										*[local-name() = 'terms']/*[local-name() = 'clause'] |
																																										*[local-name() = 'terms']/*[local-name() = 'definitions'] |
																																										*[local-name() = 'definitions']/*[local-name() = 'clause'] |
																																										*[local-name() = 'clause']/*[local-name() = 'definitions'] |
																																										*[local-name() = 'definitions']/*[local-name() = 'definitions']"/>
							</xsl:variable>
							<xsl:variable name="addon">
								<xsl:choose>
									<xsl:when test="local-name() = 'preferred' or local-name() = 'admitted' or local-name() = 'deprecates' or local-name() = 'domain'">
										<xsl:number format="-1" count="*[local-name() = 'preferred'] | *[local-name() = 'admitted'] | *[local-name() = 'deprecates'] | *[local-name() = 'domain']"/>
									</xsl:when>
									<xsl:otherwise></xsl:otherwise>
								</xsl:choose>
							</xsl:variable>
							<xsl:value-of select="concat($sectionNum, $num, $addon)"/>
							
						</xsl:when>
						<xsl:otherwise></xsl:otherwise>
					</xsl:choose>
				</xsl:when>				
				<xsl:when test="ancestor::*[local-name() = 'annex']">
					<xsl:variable name="annexid" select="normalize-space(/*/*[local-name() = 'bibdata']/*[local-name() = 'ext']/*[local-name() = 'structuredidentifier']/*[local-name() = 'annexid'])"/>
					<xsl:choose>
						<xsl:when test="local-name() = 'table'">
							<xsl:variable name="curr_annexid" select="ancestor::*[local-name() = 'annex']/@id"/>							
							<xsl:number format="A" count="*[local-name() = 'annex']"/>
							<xsl:number format=".1" level="any" count="*[local-name() = 'table'][ancestor::*[local-name() = 'annex']/@id = $curr_annexid]"/>
						</xsl:when>						
						<xsl:when test="local-name() = 'figure'">
							<xsl:number format="A.1-1" level="multiple" count="*[local-name() = 'annex'] | *[local-name() = 'figure']"/>
						</xsl:when>
						<xsl:when test="$level = 1">							
							<xsl:choose>
								<xsl:when test="count(//*[local-name() = 'annex']) = 1 and $annexid != ''">
									<xsl:value-of select="$annexid"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:number format="A" level="any" count="*[local-name() = 'annex']"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:otherwise>							
							<xsl:choose>
								<xsl:when test="count(//*[local-name() = 'annex']) = 1 and $annexid != ''">
									<xsl:value-of select="$annexid"/><xsl:number format=".1" level="multiple" count="*[local-name() = 'clause']"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:number format="A.1" level="multiple" count="*[local-name() = 'annex'] | *[local-name() = 'clause']"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:when test="ancestor::*[local-name() = 'preface']"> <!-- if preface and there is clause(s) -->
					<xsl:choose>
						<xsl:when test="$level = 1 and  ..//*[local-name() = 'clause']">0</xsl:when>
						<xsl:when test="$level &gt;= 2">
							<xsl:variable name="num">
								<xsl:number format=".1" level="multiple" count="*[local-name() = 'clause']"/>
							</xsl:variable>
							<xsl:value-of select="concat('0', $num)"/>
						</xsl:when>
						<xsl:otherwise></xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:value-of select="$section"/>
	</xsl:template>
	
</xsl:stylesheet>