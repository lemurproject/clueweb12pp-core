'''
Tool to execute an index pages crawl.
Feed pages in one by one to a running job
'''

import argparse
import os
import time


ACTION_DIRECTORY = 'action'

def load_forum_pages(forum_pages_list):
	with open(forum_pages_list, 'r') as forum_pages_list_handle:
		for new_line in forum_pages_list_handle:
			yield new_line.strip()

def download_index_pages(index_page_crawl_directory, forum):
	action_directory = os.path.join(index_page_crawl_directory, ACTION_DIRECTORY)

	with open(os.path.join(action_directory, 'index.seeds'), 'w+') as seeds_action_handle:
		seeds_action_handle.write(forum)

def is_job_empty(index_page_crawl_directory):
	job_log_path = os.path.join(index_page_crawl_directory, 'job.log')

	with open(job_log_path, 'r') as job_log_path_handle:
		last_line = None
		for new_line in job_log_path_handle:
			last_line = new_line

		return last_line.find('EMPTY') >= 0

if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('job_directory', metavar = 'job-directory', help = 'Yahoo index pages job directory')
		parser.add_argument('list_of_forums', metavar = 'list-of-forums', help = 'Yahoo group to download')

		return parser.parse_args()

	parsed = parse_cmdline_args()

	for forum in load_forum_pages(parsed.list_of_forums):
		while not is_job_empty(parsed.job_directory):
			pass

		download_index_pages(parsed.job_directory, forum)
		time.sleep(240)