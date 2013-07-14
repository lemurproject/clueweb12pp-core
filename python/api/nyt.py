'''
Code to get NYT comments
'''
import argparse
import datetime
import gzip
import json
import os
import requests
import sys
import time
import traceback
from dateutil.rrule import rrule, DAILY


NYT_COMMENTS_DATE_URL = 'http://api.nytimes.com/svc/community/v2/comments/by-date/DATE.json'
DEST_DIRECTORY = '/bos/tmp19/spalakod/clueweb12pp/jobs/nyt/'
LOG_FILE = 'nyt_dates.log'
ERR_FILE = 'nyt_err.log'

CALLS_LEFT = 0

CLUEWEB_CRAWL_START_DATE2 = datetime.date(2012, 1, 9)
CLUEWEB_CRAWL_END_DATE = datetime.date(2012, 6, 30)

def get_posts_on_date(date_str, api_key, offset = 0):
	'''
	Args:
		- date_str : date for OS X
		- api_key  : NYT Community API Key
		- offset   : API Parameter. Defaults to 0
	Returns:
		A list of posts on the specified date-string
	'''
	global CALLS_LEFT

	base_uri = NYT_COMMENTS_DATE_URL.replace('DATE', date_str)
	comments_collected = 0
	comments_list = []

	def comments_found(json_resp):
		return json_resp['results']['totalCommentsFound']

	def comments_returned(json_resp):
		return json_resp['results']['totalCommentsReturned']

	def add_comments(parsed_json):
		comments_list.extend(parsed_json['results']['comments'])

	def dump_comments():
		dest_file = os.path.join(DEST_DIRECTORY, date_str + '.gz')
		dest_file_handle = gzip.open(dest_file, 'ab')
		comment_lines = map(lambda s : json.dumps(s),comments_list)
		comment_lines = map(lambda s : s + '\n', comment_lines)
		dest_file_handle.writelines(comment_lines)
		dest_file_handle.close()

	def save_state():
		dump_comments()
		logger = open(LOG_FILE, 'a+')
		logger.write(date_str + '\t' + str(offset+25) + '\n')

	def record_err():
		err_log = open(ERR_FILE, 'a+')
		sys.stderr = err_log
		traceback.print_stack()

	while (True):
		if not CALLS_LEFT:
			time.sleep(24 * 60 * 60) # wait for a day. API Usage limits
			CALLS_LEFT = 5000

		if offset:
			uri = base_uri + '?offset=%(offset)d&api-key=%(api_key)s' % { 'offset' : offset, 'api_key' : api_key }
		else:
			uri = base_uri + '?api-key=%(api_key)s' % { 'api_key' : api_key }

		resp = requests.get(uri)
		CALLS_LEFT -= 1

		text = resp.text
		parsed_json = json.loads(text)
		
		comments_collected += comments_returned(parsed_json)
		add_comments(parsed_json)

		if comments_found(parsed_json) > comments_collected:
			offset += comments_returned(parsed_json)
			time.sleep(0.04)
		else:
			dump_comments()
			return


if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('api')
		parser.add_argument('--remaining', dest = 'remaining', type = int, help = 'Number of calls left in the day', default = 5000)

		return parser.parse_args()

	parsed = parse_cmdline_args()

	start_date = CLUEWEB_CRAWL_START_DATE2
	offset = 0
	CALLS_LEFT = parsed.remaining
	
	for dt in rrule(DAILY, dtstart = start_date, until = CLUEWEB_CRAWL_END_DATE):
		dt_str = dt.strftime("%Y%m%d")
		for _ in range(10):
			try:
				get_posts_on_date(dt_str, parsed.api, offset)
				break
			except:
				continue