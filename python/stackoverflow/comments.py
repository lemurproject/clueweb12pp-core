'''
Stackoverflow comments.xml data
'''

import argparse
import datetime
import xml.sax
from dateutil.parser import parse

CLUEWEB_START = datetime.datetime(2012, 01, 01)
CLUEWEB_END = datetime.datetime(2012, 06, 30)


class StackOverflowContentHandler(xml.sax.ContentHandler):
	def __init__(self):
		xml.sax.ContentHandler.__init__(self)

	def startElement(self, name, attrs):
		self.process_row(name, attrs)

	def endElement(self, name):
		if name == 'posts':
			print 'Number of posts:', self.post_count
			print 'Number of posts overall:', self.question_count
			print 'Number of posts with answers:', self.question_with_answer_count
			print 'Number of posts with comments:', self.comment_post_count

	def process_row(self, name, attrs):
		if name != 'row':
			return
		creation_date = parse(attrs.get('CreationDate'))

		if creation_date <= CLUEWEB_END and creation_date >= CLUEWEB_START:

			print attrs.get('UserId')


if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('comments_file', metavar = 'comments-file')

		return parser.parse_args()

	parsed = parse_cmdline_args()

	source = open(parsed.comments_file)

	xml.sax.parse(source, StackOverflowContentHandler())