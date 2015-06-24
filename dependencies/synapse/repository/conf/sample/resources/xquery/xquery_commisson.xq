<x><![CDATA[
  declare namespace ns="http://services.samples";
  declare namespace m1="http://services.samples/xsd";
  declare variable $payload as document-node() external;
  declare variable $commission as document-node() external;
  <m0:return xmlns:m0="http://services.samples/xsd">
  	<m0:symbol>{$payload//ns:return/m1:symbol/child::text()}</m0:symbol>
  	<m0:last>{$payload//ns:return/m1:last/child::text()+ $commission//commission/vendor[@symbol=$payload//ns:return/m1:symbol/child::text()]}</m0:last>
  </m0:return>  
]]></x>