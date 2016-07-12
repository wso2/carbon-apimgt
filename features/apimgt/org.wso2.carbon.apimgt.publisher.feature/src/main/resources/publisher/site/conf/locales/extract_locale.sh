#/bin/bash
cd ../../../

# Create file list to generate pot files
find . | grep "\.jag$" > jag_files.list
find site/themes/wso2/templates/| grep -v "\.min\.js$" | grep "\.js$" > js_files.list

# Generate pot files
xgettext --language=PHP --keyword=localize:1 --from-code=utf-8 --force-po -c -o site/conf/locales/jaggery.pot  -f jag_files.list 
xgettext --language=JavaScript --keyword=t:1 --from-code=utf-8 --force-po -c -o site/conf/locales/javascript.pot  -f js_files.list 

#merge the dynamic values
msgcat site/conf/locales/jaggery.pot site/conf/locales/dynamic.po > site/conf/locales/jaggery.pot_tmp
msgcat site/conf/locales/javascript.pot site/conf/locales/dynamic.po > site/conf/locales/javascript.pot_tmp
mv site/conf/locales/jaggery.pot_tmp site/conf/locales/jaggery.pot
mv site/conf/locales/javascript.pot_tmp site/conf/locales/javascript.pot

# Generate json files
i18next-conv -l -s site/conf/locales/jaggery.pot -t site/conf/locales/jaggery/locale_en.json
i18next-conv -l -s site/conf/locales/javascript.pot -t site/conf/locales/js/i18nResources.json

python - <<END
import json

filename = 'site/conf/locales/jaggery/locale_en.json'

print('Generating default json')

f = open(filename , 'r+')
text = f.read()
f.close();

data = json.loads(text)
for key in data:
    if data[key] == "":
        data[key] = key

#JSON encode with pretty print
json_text = json.dumps(data ,sort_keys=True , indent=4, separators=(',', ': '))

#Write to file
with open(filename, "w") as f:
    f.write(json_text)

filename = 'site/conf/locales/js/i18nResources.json'

print('Generating default json for javascript')

f = open(filename , 'r+')
text = f.read()
f.close();

data = json.loads(text)
for key in data:
    if data[key] == "":
        data[key] = key

#JSON encode with pretty print
json_text = json.dumps(data ,sort_keys=True , indent=4, separators=(',', ': '))

#Write to file
with open(filename, "w") as f:
    f.write(json_text)
END
cp site/conf/locales/jaggery/locale_en.json site/conf/locales/jaggery/locale_default.json
rm jag_files.list
rm js_files.list

msgcat site/conf/locales/jaggery.pot site/conf/locales/javascript.pot > site/conf/locales/master.pot

