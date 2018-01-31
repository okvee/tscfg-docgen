<!--
  WARNING: the configuration documentation below was generated automatically,
           any manual changes may get overwritten
-->

<#macro escapeMarkdown string>${string?replace("|", "&#124;")}</#macro>

<#macro configTable keyValueList>
| Name | Description | Value type | Default value |
| :--- | :---------- | :--------- | :------------ |
<#list keyValueList as kv>
| <code><@escapeMarkdown string=kv.key /></code> | <@escapeMarkdown string=kv.description /> | ${kv.valueType} | <code><@escapeMarkdown string=kv.defaultValue /></code> |
</#list>
</#macro>

<#list groups as group>
${group.heading}

<@configTable keyValueList=group.keyValues />

<#else>
<@configTable keyValueList=keyValues />

</#list>