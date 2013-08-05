'''
Diqus posts download
'''

import argparse
import gzip
import json
import sys
import time
import traceback
from disqusapi import DisqusAPI, Paginator, APIError

FAILED_IDS = 'disqus_failed_posts.txt'
FAILED_IDS_HANDLE = open(FAILED_IDS, 'a+')


def load_downloaded_ids(downloaded):
	downloaded_ids = set()
	with open(downloaded, 'r') as downloaded_handle:
		for new_line in downloaded_handle:
			downloaded_ids.add(new_line.strip())

	return downloaded_ids

def process_disqus_file(disqus_file, downloaded_file = None):
	'''
	process file
	'''
	downloaded_ids = load_downloaded_ids(downloaded_file)

	f = gzip.open(disqus_file, 'r')

	while True:
		new_line = f.readline()

		if not new_line:
			return

		parsed = json.loads(new_line)
		if parsed['posts'] > 0:
			if not parsed['id'] in downloaded_ids:
				print parsed['id']
				sys.stdout.flush()

def save_disqus_posts(ids_file, destination, api):
	'''
	Retrieve all posts in the thread given by thread-id
	'''
	f = gzip.open(destination, 'ab')

	with open(ids_file, 'r') as ids_stream:
		for new_id in ids_stream:
			success = False
			for _ in range(10):
				try:
					save_disqus_posts_for_thread(new_id.strip(), f, api)
					success = True
					break
				except APIError as err:
					# redownloading is the only choice.
					# Wait out 2000 seconds since the API doesn't
					# tell us how many seconds we need to wait for
					if err.code == 2:
						#sys.stderr.write(new_id)
						continue # forum doesn't exist so just iterate out

					sys.stderr.write('INFO: APIError' + '\n')
					traceback.print_stack()
					time.sleep(2000)
					continue
				except:
					# retry downloading
					continue
			if success:
				print new_id.strip()
				sys.stdout.flush()
			else:
				sys.stderr.write(new_id.strip() + '\n')
	f.close()

def save_disqus_posts_for_thread(thread_id, destination_handle, api):
	paginator = Paginator(api.threads.listPosts, thread = thread_id)
	for result in paginator():
		destination_handle.write(json.dumps(result) + '\n')

if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('--secret-key', dest = 'secret_key')
		parser.add_argument('--public-key', dest = 'public_key')

		parser.add_argument('-d', '--dump-ids', dest = 'dump_ids', default = [], nargs = '+', help = 'Pass in a posts.gz file for us to obtain a list of posts')
		parser.add_argument('-s', '--save-posts', dest = 'save_posts', default = None, help = 'Pass in a list of ids and we download the posts')
		parser.add_argument('--destination', dest = 'destination', help = 'Where to store the resulting posts. Name must end in .gz')
		parser.add_argument('--downloaded', dest = 'downloaded', default = None, help = 'Log file of previous download. We use this to sift out already downloaded ids')
		parser.add_argument('--failed-ids', dest = 'failed_ids', default = None, help = 'Store ids that failed')

		return parser.parse_args()

	parsed = parse_cmdline_args()

	if parsed.dump_ids:
		for filename in parsed.dump_ids:
			process_disqus_file(filename, parsed.downloaded)

	elif parsed.save_posts:
		FAILED_IDS = parsed.failed_ids
		FAILED_IDS_HANDLE = open(FAILED_IDS, 'a+')
		#sys.stderr = FAILED_IDS_HANDLE
		api = DisqusAPI(parsed.secret_key, parsed.public_key)
		save_disqus_posts(parsed.save_posts, parsed.destination, api)