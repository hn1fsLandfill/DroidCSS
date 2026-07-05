import base64
import requests
import xml.etree.ElementTree as ET

BRANCH = "android16-qpr2-release"
PUBLIC = f"https://android.googlesource.com/platform/frameworks/base/+/refs/heads/{BRANCH}/core/res/res/values/public-final.xml?format=TEXT"

rawrequest = requests.get(PUBLIC)
root = ET.fromstring(base64.b64decode(rawrequest.text))

attrs = {}

for child in root:
	if child.tag == "public" and child.attrib["type"] == "attr":
		attrs[child.attrib["name"]] = child.attrib["id"]

# generate final result
print("package eu.hn1f.droidcss;")

print("class androidStyleableAttrs {")
for attr in attrs.keys():
	print(f"public static int {attr} = {attrs[attr]};")
print("}")
