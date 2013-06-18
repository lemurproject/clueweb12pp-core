'''
Code to process nabble posts and check if they lie within our time-frame
'''

import argparse
import clueweb12pp_core
import datetime
import os
import re
import sys
from BeautifulSoup import BeautifulSoup


POSTS_IN_TIME_RANGE = set() # contains a list of post URLS
FAILED_POSTS = 0

STATS_OUTPUT_FILE = 'nabble_posts_stats.txt'
POST_LINKS_OUTPUT_FILE = 'nabble_posts_links.txt'

def process_nabble_post_directory(nabble_post_directory):
	'''
	Process a directory
	'''
	for warc_file in clueweb12pp_core.job_warc_files(nabble_post_directory):
		handle_warc_file(warc_file)
		print 'Finished Processing:', warc_file
		print 'Posts in Time Range:', len(POSTS_IN_TIME_RANGE)

def handle_warc_file(warc_file):
	'''
	Process a nabble
	'''
	global FAILED_POSTS
	global POSTS_IN_TIME_RANGE

	def is_redirect(record_payload):
		payload = record_payload.splitlines()
		return payload[0].find('301') >= 0 or payload[0].find('302') >= 0

	def fix_js(js_string_expr):
		return re.search(r'.*new Date\((\d+)\)', js_string_expr).group(1)
	try:
		for record in clueweb12pp_core.get_content_warcs(warc_file):
			record_payload = record.payload.read()
			if not is_redirect(record_payload):
				soup = BeautifulSoup(record_payload)
				date_tags = soup.findAll('span', {'class' : 'post-date float-left'})
				for tag in date_tags:
					seconds_since_epoch = int(fix_js(tag.script.text))/1000
					if clueweb12pp_core.is_within_crawl_timespan(datetime.datetime.fromtimestamp(seconds_since_epoch)):
						POSTS_IN_TIME_RANGE.add(record.url)
					else:
						FAILED_POSTS += 1
	except IOError:
		print 'Failed to fully process: ', warc_file
		return

def write_output():
	with open(STATS_OUTPUT_FILE, 'w+') as stats_handle:
		stats_handle.write('Posts not in our time range: %(failed_posts)d' % {'failed_posts' : FAILED_POSTS})
		stats_handle.write('\n')
		stats_handle.flush()

	with open(POST_LINKS_OUTPUT_FILE, 'w+') as posts_handle:
		for post_url in POSTS_IN_TIME_RANGE:
			posts_handle.write(post_url)
			posts_handle.write('\n')
			posts_handle.flush()

if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('nabble_post_directories', metavar = 'nabble-post-directories', nargs = '+')
		parser.add_argument('--output-directory', dest = 'output_directory', default = '.', help = 'Where to put the output files')

		return parser.parse_args()

	parsed = parse_cmdline_args()

	STATS_OUTPUT_FILE = os.path.join(parsed.output_directory, STATS_OUTPUT_FILE)
	POST_LINKS_OUTPUT_FILE = os.path.join(parsed.output_directory, POST_LINKS_OUTPUT_FILE)

	reload(sys)
	sys.setdefaultencoding("utf-8")

	for nabble_post_directory in parsed.nabble_post_directories:
		process_nabble_post_directory(nabble_post_directory)

	write_output()