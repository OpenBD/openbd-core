<cfinclude template="header.inc">

<!--- Single Clip --->
<cfset singleClip = "http://blip.tv/file/get/N8inpasadena-Flowers457.flv">

<div align="center">
  <cfvideoplayer video="#singleClip#" autoplay="false">
</div>

<!--- Multiple Clips --->
<cfset clips = ArrayNew(1)>
<cfset clips[1] = "http://blip.tv/file/get/Commstrat-DrunkFlowers989.flv">
<cfset clips[2] = "http://blip.tv/file/get/N8inpasadena-Flowers457.flv">

<p>&nbsp;</p>

<div align="center">
  <cfvideoplayer video="#clips#" height="330" width="400" playlist="true" stop="true" autoplay="false">
</div>

<cfinclude template="footer.inc">