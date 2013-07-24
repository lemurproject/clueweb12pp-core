'''
Diqus posts download
'''

import argparse
import gzip
import json
import sys
import time
import traceback

import clueweb_disqus_process
from disqusapi import DisqusAPI, Paginator, APIError


FAILED_IDS = None
FAILED_IDS_HANDLE = None

def process_disqus_file(disqus_file, downloaded_file):
	'''
	process file
	'''

	downloaded_ids = []
	if not downloaded_file is None:
		with open(downloaded_file, 'r') as handle:
			for new_line in handle:
				downloaded_ids.append(new_line.strip())

	downloaded_ids = set(downloaded_ids)

	for post_line in clueweb_disqus_process.iter_clueweb_posts_raw(disqus_file):
		try:
			parsed = json.loads(post_line)
			if parsed['id'] not in downloaded_ids and parsed['posts'] > 0:
				print parsed['id']
		except:
			continue

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

		parser.add_argument('-d', '--dump-ids', dest = 'dump_ids', nargs = '+', help = 'Pass in a posts.gz file for us to obtain a list of posts')
		parser.add_argument('-s', '--save-posts', dest = 'save_posts', default = None, help = 'Pass in a list of ids and we download the posts')
		parser.add_argument('-f', '--failed-ids', dest = 'failed_ids', default = 'disqus_failed_posts.txt')
		parser.add_argument('--downloaded', dest = 'downloaded', default = None)
		parser.add_argument('--destination', dest = 'destination', help = 'Where to store the resulting posts. Name must end in .gz')

		return parser.parse_args()

	parsed = parse_cmdline_args()

	if parsed.dump_ids:
		for dump_id_file in parsed.dump_ids:
			if parsed.downloaded:
				process_disqus_file(dump_id_file, parsed.downloaded)
			else:
				process_disqus_file(dump_id_file, None)

	elif parsed.save_posts:
		FAILED_IDS = parsed.failed_ids
		FAILED_IDS_HANDLE = open(FAILED_IDS, 'a+')
		sys.stderr = FAILED_IDS_HANDLE

		api = DisqusAPI(parsed.secret_key, parsed.public_key)
		save_disqus_posts(parsed.save_posts, parsed.destination, api)