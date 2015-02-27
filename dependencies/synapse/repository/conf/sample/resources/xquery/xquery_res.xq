<x><![CDATA[
  declare namespace m0="http://services.samples/xsd";
  declare variable $payload as document-node() external;
  declare variable $code as xs:string external;
  declare variable $price as xs:double external;
  <m:CheckPriceResponse xmlns:m="http://services.samples/xsd">
  	<m:Code>{$code}</m:Code>
  	<m:Price>{$price}</m:Price>
  </m:CheckPriceResponse>
]]></x>