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
	def __init__(self):
		self.comment_post_count = 0 # keeps track of posts with comments
		self.question_count = 0 # keep track of asked questions
		self.question_with_answer_count = 0 # keep track of asked questions that got answers
		self.post_count = 0
		xml.sax.ContentHandler.__init__(self)

	def startElement(self, name, attrs):
		self.process_post_row(name, attrs)

	def endElement(self, name):
		if name == 'posts':
			print 'Number of posts:', self.post_count
			print 'Number of questions:', self.question_count
			print 'Number of questions with answers:', self.question_with_answer_count
			print 'Number of posts with comments:', self.comment_post_count

	def process_post_row(self, name, attrs):
		if name != 'row':
			return

		creation_date = parse(attrs.get('CreationDate'))

		if creation_date <= CLUEWEB_END and creation_date >= CLUEWEB_START:

			self.post_count += 1

			if int(attrs.get('PostTypeId')) == 1:
				self.question_count += 1

				try:
					if int(attrs.get('AnswerCount')) > 0:
						self.question_with_answer_count += 1
				except:
					pass

			try:
				if int(attrs.get('CommentCount')) > 0:
					self.comment_post_count += 1
			except:
				pass

if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('posts_file', metavar = 'posts-file')
		parser.add_argument('--stats', dest = 'stats', action = 'store_true', default = False)
		return parser.parse_args()

	parsed = parse_cmdline_args()

	source = open(parsed.posts_file)

	if parsed.stats:
		xml.sax.parse(source, StackOverflowContentHandler())