# Use an official Python runtime as a parent image
FROM python:3.11-slim

# Set the working directory in the container
WORKDIR /app

RUN rm -rf /var/lib/apt/lists/* && \
    apt-get clean && \
    apt-get update && \
    apt-get install -y libgl1 libglib2.0-0 && \
    rm -rf /var/lib/apt/lists/*

	
# Copy the requirements file and install dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the application code into the container
COPY . .
# Overlap modified aura_sr.py to local directory 
COPY aura_sr.py /usr/local/lib/python3.11/site-packages/aura_sr.py
# Expose the port FastAPI will run on
EXPOSE 8000

# Command to run the FastAPI application using Uvicorn
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
