'''
Code to process Posts.xml in raw StackOverflow data
'''

import argparse
import datetime
import xml.sax
from dateutil.parser import parse

CLUEWEB_START = datetime.datetime(2012, 01, 01)
CLUEWEB_END = datetime.datetime(2012, 06, 30)


class StackOverflowContentHandler(xml.sax.ContentHandler):
	def __init__(self, f_row):
		'''
		Args:
			- f_row : Function run on each row element.
					  Function accepts two args: name and attrs
		'''
		self.f_row = f_row
		self.count = 0
		xml.sax.ContentHandler.__init__(self)

	def startElement(self, name, attrs):
		if self.f_row(name, attrs):
			self.count += 1

	def endElement(self, name):
		if name == 'posts':
			print 'Number of posts:', self.count

def count_posts_in_time_range(element_name, element_attrs):
	'''
	Args:
		- element_name : String representation of the node.
		- element_attrs : Processes attributes of a row node.
	'''

	if element_name == 'row':
		date_str = element_attrs.get('CreationDate')
		return parse(date_str) > CLUEWEB_START and parse(date_str) < CLUEWEB_END



if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('posts_file', metavar = 'posts-file')
		parser.add_argument('--count-posts', dest = 'count_posts', action = 'store_true', default = False)

		return parser.parse_args()

	parsed = parse_cmdline_args()

	source = open(parsed.posts_file)

	if parsed.count_posts:
		xml.sax.parse(source, StackOverflowContentHandler(count_posts_in_time_range))
